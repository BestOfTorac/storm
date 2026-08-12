package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.inventory.JavaInventoryCsvReader;
import it.uniroma2.isw2.storm.model.JavaFileInventoryEntry;
import it.uniroma2.isw2.storm.model.SonarSmellMetric;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.sonar.SonarCloudClient;
import it.uniroma2.isw2.storm.sonar.SonarSmellCsvWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SonarCloudSmellGenerator {

    private static final Pattern PARSE_ERROR_PATTERN =
        Pattern.compile(
            "Unable to parse source file\\s*:\\s*'([^']+)'"
        );
    private SonarCloudSmellGenerator() {
        // Application entry point.
    }

    public static void main(String[] args) {
        int exitCode = run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        if (args.length != 5) {
            System.err.println(
                "Usage: SonarCloudSmellGenerator "
                    + "<repository-path> "
                    + "<sonar-scanner-executable> "
                    + "<organization> "
                    + "<project-key> "
                    + "<release-index|all>"
            );
            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path scannerExecutable =
            Path.of(args[1])
                .toAbsolutePath()
                .normalize();

        String organization = args[2].strip();
        String projectKey = args[3].strip();
        String selector = args[4].strip();

        Path inventoryPath =
            repositoryPath.resolve(
                "isw2/datasets/java_class_inventory.csv"
            );

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/sonar_smell_metrics_pilot.csv"
            );

        Path temporaryRoot = null;

        try {
            validateArguments(
                repositoryPath,
                scannerExecutable,
                organization,
                projectKey,
                selector
            );

            List<JavaFileInventoryEntry> production =
                JavaInventoryCsvReader.read(inventoryPath)
                    .stream()
                    .filter(
                        row ->
                            row.sourceCategory()
                                == SourceCategory.PRODUCTION
                    )
                    .sorted(
                        Comparator
                            .comparingInt(
                                JavaFileInventoryEntry
                                    ::releaseIndex
                            )
                            .thenComparing(
                                JavaFileInventoryEntry
                                    ::filePath
                            )
                    )
                    .toList();

            Map<Integer, List<JavaFileInventoryEntry>>
                byRelease = groupByRelease(production);

            List<Integer> releases =
                selectReleases(
                    byRelease,
                    selector
                );

            SonarCloudClient client =
                SonarCloudClient.fromEnvironment();

            temporaryRoot =
                Files.createTempDirectory(
                    "storm-sonar-smells-"
                );

            List<SonarSmellMetric> output =
                new ArrayList<>();

            int position = 0;

            for (int releaseIndex : releases) {
                position++;

                List<JavaFileInventoryEntry> releaseRows =
                    byRelease.get(releaseIndex);

                JavaFileInventoryEntry representative =
                    releaseRows.get(0);

                String version = representative.version();
                String commitId = representative.commitId();

                validateReleaseRows(
                    releaseRows,
                    version,
                    commitId
                );

                System.out.printf(
                    "[%d/%d] %-18s classes=%d%n",
                    position,
                    releases.size(),
                    version,
                    releaseRows.size()
                );

                Path worktree =
                    temporaryRoot.resolve(
                        "worktree-" + releaseIndex
                    );

                Path staging =
                    temporaryRoot.resolve(
                        "sonar-input-" + releaseIndex
                    );

                boolean worktreeCreated = false;

                try {
                    addWorktree(
                        repositoryPath,
                        worktree,
                        commitId
                    );
                    worktreeCreated = true;

                    stageProductionFiles(
                        worktree,
                        staging,
                        releaseRows
                    );

                    ScannerDiagnostics diagnostics =
                        runScanner(
                            scannerExecutable,
                            staging,
                            organization,
                            projectKey,
                            version
                        );

                    String ceTaskId =
                        readCeTaskId(
                            staging.resolve(
                                ".scannerwork/report-task.txt"
                            )
                        );

                    client.waitForCeTask(ceTaskId);

                    if (!diagnostics
                            .unresolvedMessages()
                            .isEmpty()) {

                        String sample =
                            diagnostics
                                .unresolvedMessages()
                                .stream()
                                .limit(10)
                                .collect(
                                    Collectors.joining(
                                        System.lineSeparator()
                                    )
                                );

                        throw new IOException(
                            "SonarJava still has "
                                + diagnostics
                                    .unresolvedMessages()
                                    .size()
                                + " unresolved entries."
                                + System.lineSeparator()
                                + sample
                        );
                    }

                    Map<String, Integer> smellsByPath =
                        client.fetchCodeSmells(
                            projectKey
                        );

                    List<SonarSmellMetric> releaseMetrics =
                        joinRelease(
                            releaseRows,
                            smellsByPath,
                            diagnostics.parseErrorPaths()
                        );

                    long validatedTotalSmells =
                        releaseMetrics.stream()
                            .mapToLong(
                                SonarSmellMetric::nSmells
                            )
                            .sum();

                    int projectCodeSmells =
                        client.fetchProjectCodeSmells(
                            projectKey
                        );

                    if (validatedTotalSmells
                            != projectCodeSmells) {

                        throw new IOException(
                            "File-level NSMELLS sum ("
                                + validatedTotalSmells
                                + ") differs from SonarCloud "
                                + "project code_smells ("
                                + projectCodeSmells
                                + ")."
                        );
                    }

                    System.out.printf(
                        "       validation: "
                            + "project-smells=%d, "
                            + "parse-errors=%d, "
                            + "unresolved=%d%n",
                        projectCodeSmells,
                        diagnostics
                            .parseErrorPaths()
                            .size(),
                        diagnostics
                            .unresolvedMessages()
                            .size()
                    );

                    output.addAll(releaseMetrics);

                    // Keep a durable checkpoint after every release.
                    // If a later SonarCloud analysis fails, the releases
                    // already completed are still available on disk.
                    SonarSmellCsvWriter.write(
                        outputPath,
                        output
                    );

                    long withSmells =
                        releaseMetrics.stream()
                            .filter(
                                row -> row.nSmells() > 0
                            )
                            .count();

                    long totalSmells =
                        releaseMetrics.stream()
                            .mapToLong(
                                SonarSmellMetric::nSmells
                            )
                            .sum();

                    int maximum =
                        releaseMetrics.stream()
                            .mapToInt(
                                SonarSmellMetric::nSmells
                            )
                            .max()
                            .orElse(0);

                    System.out.printf(
                        "       sonar-files=%d, "
                            + "classes-with-smells=%d, "
                            + "total-smells=%d, max=%d%n",
                        smellsByPath.size(),
                        withSmells,
                        totalSmells,
                        maximum
                    );

                } finally {
                    deleteRecursively(staging);

                    if (worktreeCreated) {
                        removeWorktree(
                            repositoryPath,
                            worktree
                        );
                    }
                }
            }

            output.sort(
                Comparator
                    .comparingInt(
                        SonarSmellMetric::releaseIndex
                    )
                    .thenComparing(
                        SonarSmellMetric::filePath
                    )
            );

            SonarSmellCsvWriter.write(
                outputPath,
                output
            );

            System.out.println();
            System.out.printf(
                "SonarCloud smell observations: %d%n",
                output.size()
            );
            System.out.printf(
                "SonarCloud smell metrics written to: %s%n",
                outputPath
            );

            return 0;

        } catch (Exception exception) {
            System.err.printf(
                "SonarCloud smell generation failed: %s%n",
                exception.getMessage()
            );
            exception.printStackTrace(System.err);
            return 1;

        } finally {
            if (temporaryRoot != null) {
                try {
                    deleteRecursively(temporaryRoot);
                } catch (IOException ignored) {
                    // Best-effort cleanup.
                }
            }
        }
    }

    private static void validateArguments(
            Path repositoryPath,
            Path scannerExecutable,
            String organization,
            String projectKey,
            String selector)
            throws IOException {

        if (!Files.isDirectory(repositoryPath)) {
            throw new IOException(
                "Repository not found: "
                    + repositoryPath
            );
        }

        if (!Files.isRegularFile(scannerExecutable)) {
            throw new IOException(
                "SonarScanner executable not found: "
                    + scannerExecutable
            );
        }

        if (organization.isBlank()) {
            throw new IllegalArgumentException(
                "SonarCloud organization cannot be blank."
            );
        }

        if (projectKey.isBlank()) {
            throw new IllegalArgumentException(
                "SonarCloud project key cannot be blank."
            );
        }

        if (!"all".equalsIgnoreCase(selector)) {
            try {
                int release = Integer.parseInt(selector);
                if (release <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                    "Release selector must be a positive integer or 'all'."
                );
            }
        }

        String token = System.getenv("SONAR_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                "Environment variable SONAR_TOKEN is not set."
            );
        }
    }

    private static Map<Integer, List<JavaFileInventoryEntry>>
            groupByRelease(
                List<JavaFileInventoryEntry> production) {

        Map<Integer, List<JavaFileInventoryEntry>> result =
            new LinkedHashMap<>();

        for (JavaFileInventoryEntry row : production) {
            result
                .computeIfAbsent(
                    row.releaseIndex(),
                    ignored -> new ArrayList<>()
                )
                .add(row);
        }

        return result;
    }

    private static List<Integer> selectReleases(
            Map<Integer, List<JavaFileInventoryEntry>> byRelease,
            String selector) {

        if ("all".equalsIgnoreCase(selector)) {
            return List.copyOf(byRelease.keySet());
        }

        int release = Integer.parseInt(selector);

        if (!byRelease.containsKey(release)) {
            throw new IllegalArgumentException(
                "Release index not found in production inventory: "
                    + release
            );
        }

        return List.of(release);
    }

    private static void validateReleaseRows(
            List<JavaFileInventoryEntry> rows,
            String expectedVersion,
            String expectedCommit)
            throws IOException {

        for (JavaFileInventoryEntry row : rows) {
            if (!expectedVersion.equals(row.version())) {
                throw new IOException(
                    "Multiple versions found for release "
                        + row.releaseIndex()
                );
            }

            if (!expectedCommit.equals(row.commitId())) {
                throw new IOException(
                    "Multiple commits found for release "
                        + row.releaseIndex()
                );
            }
        }
    }

    private static void stageProductionFiles(
            Path worktree,
            Path staging,
            List<JavaFileInventoryEntry> rows)
            throws IOException {

        Files.createDirectories(staging);

        for (JavaFileInventoryEntry row : rows) {
            Path source =
                worktree.resolve(row.filePath()).normalize();

            if (!source.startsWith(worktree)
                    || !Files.isRegularFile(source)) {
                throw new IOException(
                    "Production source file not found: "
                        + row.filePath()
                );
            }

            Path target =
                staging.resolve(row.filePath()).normalize();

            if (!target.startsWith(staging)) {
                throw new IOException(
                    "Invalid production file path: "
                        + row.filePath()
                );
            }

            Files.createDirectories(target.getParent());
            Files.copy(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private static ScannerDiagnostics runScanner(
            Path scannerExecutable,
            Path staging,
            String organization,
            String projectKey,
            String version)
            throws IOException, InterruptedException {

        String javaSource =
            requireEnvironmentVariable(
                "STORM_SONAR_JAVA_SOURCE"
            );

        Path jdkHome =
            requireDirectoryFromEnvironment(
                "STORM_SONAR_JDK_HOME"
            );

        Path stormJarDirectory =
            requireJarDirectoryFromEnvironment(
                "STORM_SONAR_STORM_JARS"
            );

        Path binaryRoot =
            requireDirectoryFromEnvironment(
                "STORM_SONAR_BINARIES_ROOT"
            );

        Path libraryDirectory =
            requireJarDirectoryFromEnvironment(
                "STORM_SONAR_LIBRARIES"
            );

        List<Path> binaryDirectories;

        try (var directories =
                Files.list(binaryRoot)) {

            binaryDirectories =
                directories
                    .filter(Files::isDirectory)
                    .sorted()
                    .toList();
        }

        if (binaryDirectories.isEmpty()) {
            throw new IOException(
                "No Sonar Java binary directories found under "
                    + binaryRoot
            );
        }

        String binaries =
            binaryDirectories.stream()
                .map(
                    SonarCloudSmellGenerator::sonarPath
                )
                .collect(
                    Collectors.joining(",")
                );

        String libraries =
            sonarPath(stormJarDirectory)
                + "/*.jar,"
                + sonarPath(libraryDirectory)
                + "/*.jar";

        List<String> scannerArguments =
            List.of(
                "-X",
                "-Dsonar.host.url=https://sonarcloud.io",
                "-Dsonar.organization=" + organization,
                "-Dsonar.projectKey=" + projectKey,
                "-Dsonar.projectVersion=" + version,
                "-Dsonar.projectName=Apache Storm ISW2",
                "-Dsonar.sources=.",
                "-Dsonar.sourceEncoding=UTF-8",
                "-Dsonar.java.source=" + javaSource,
                "-Dsonar.java.jdkHome="
                    + sonarPath(jdkHome),
                "-Dsonar.java.binaries="
                    + binaries,
                "-Dsonar.java.libraries="
                    + libraries,
                "-Dsonar.scm.disabled=true"
            );

        List<String> command =
            scannerCommand(
                scannerExecutable,
                scannerArguments
            );

        Process process =
            new ProcessBuilder(command)
                .directory(staging.toFile())
                .redirectErrorStream(true)
                .start();

        Set<String> parseErrorPaths =
            new LinkedHashSet<>();

        Set<String> unresolvedMessages =
            new LinkedHashSet<>();

        try (BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )) {

            String line;

            while ((line = reader.readLine())
                    != null) {

                System.out.println(line);

                Matcher matcher =
                    PARSE_ERROR_PATTERN.matcher(
                        line
                    );

                if (matcher.find()) {
                    parseErrorPaths.add(
                        normalizePath(
                            matcher.group(1)
                        )
                    );
                }

                if (line.contains(
                        "cannot be resolved")) {

                    unresolvedMessages.add(
                        line.strip()
                    );
                }
            }
        }

        int exitCode =
            process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                "SonarScanner failed with exit code "
                    + exitCode
            );
        }

        return new ScannerDiagnostics(
            Set.copyOf(parseErrorPaths),
            Set.copyOf(unresolvedMessages)
        );
    }

    private static String requireEnvironmentVariable(
            String name) {

        String value =
            System.getenv(name);

        if (value == null
                || value.isBlank()) {

            throw new IllegalStateException(
                "Environment variable "
                    + name
                    + " is not set."
            );
        }

        return value.strip();
    }

    private static Path requireDirectoryFromEnvironment(
            String name)
            throws IOException {

        Path directory =
            Path.of(
                requireEnvironmentVariable(name)
            )
            .toAbsolutePath()
            .normalize();

        if (!Files.isDirectory(directory)) {
            throw new IOException(
                name
                    + " directory not found: "
                    + directory
            );
        }

        return directory;
    }

    private static Path requireJarDirectoryFromEnvironment(
            String name)
            throws IOException {

        Path directory =
            requireDirectoryFromEnvironment(
                name
            );

        boolean hasJar;

        try (var files =
                Files.list(directory)) {

            hasJar =
                files.anyMatch(
                    path ->
                        Files.isRegularFile(path)
                            && path
                                .getFileName()
                                .toString()
                                .toLowerCase()
                                .endsWith(".jar")
                );
        }

        if (!hasJar) {
            throw new IOException(
                name
                    + " contains no JAR files: "
                    + directory
            );
        }

        return directory;
    }

    private static String sonarPath(Path path) {
        return path
            .toAbsolutePath()
            .normalize()
            .toString()
            .replace('\\', '/');
    }

    private static List<String> scannerCommand(
            Path scannerExecutable,
            List<String> arguments) {

        String executable = scannerExecutable.toString();
        String lower = executable.toLowerCase();

        List<String> command = new ArrayList<>();

        if (isWindows()
                && (lower.endsWith(".bat")
                    || lower.endsWith(".cmd"))) {

            command.add(windowsCommandInterpreter());
            command.add("/d");
            command.add("/c");
            command.add(executable);
        } else {
            command.add(executable);
        }

        command.addAll(arguments);
        return command;
    }

    private static String windowsCommandInterpreter() {

        String comSpec =
            System.getenv("ComSpec");

        if (comSpec != null
                && !comSpec.isBlank()
                && Files.isRegularFile(
                    Path.of(comSpec))) {

            return comSpec;
        }

        String systemRoot =
            System.getenv("SystemRoot");

        if (systemRoot != null
                && !systemRoot.isBlank()) {

            Path candidate =
                Path.of(
                    systemRoot,
                    "System32",
                    "cmd.exe"
                );

            if (Files.isRegularFile(candidate)) {
                return candidate.toString();
            }
        }

        return "C:\\Windows\\System32\\cmd.exe";
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "")
            .toLowerCase()
            .contains("win");
    }

    private static String readCeTaskId(Path reportTask)
            throws IOException {

        if (!Files.isRegularFile(reportTask)) {
            throw new IOException(
                "SonarScanner report-task.txt not found: "
                    + reportTask
            );
        }

        Properties properties = new Properties();

        try (InputStream input =
                Files.newInputStream(reportTask)) {
            properties.load(
                new java.io.InputStreamReader(
                    input,
                    StandardCharsets.UTF_8
                )
            );
        }

        String ceTaskId =
            properties.getProperty("ceTaskId", "").strip();

        if (ceTaskId.isBlank()) {
            throw new IOException(
                "ceTaskId missing from "
                    + reportTask
            );
        }

        return ceTaskId;
    }

    private static List<SonarSmellMetric> joinRelease(
            List<JavaFileInventoryEntry> releaseRows,
            Map<String, Integer> smellsByPath,
            Set<String> parseErrorPaths)
            throws IOException {

        Map<String, JavaFileInventoryEntry> expected =
            new LinkedHashMap<>();

        for (JavaFileInventoryEntry row : releaseRows) {
            String path = normalizePath(row.filePath());

            if (expected.put(path, row) != null) {
                throw new IOException(
                    "Duplicate production inventory path: "
                        + path
                );
            }
        }

        List<String> unexpected =
            smellsByPath.keySet().stream()
                .filter(path -> !expected.containsKey(path))
                .sorted()
                .toList();

        if (!unexpected.isEmpty()) {
            throw new IOException(
                "SonarCloud returned files outside the "
                    + "production inventory. First unexpected path: "
                    + unexpected.get(0)
            );
        }

        List<String> missing =
            expected.keySet().stream()
                .filter(path -> !smellsByPath.containsKey(path))
                .sorted()
                .toList();

        if (!missing.isEmpty()) {
            throw new IOException(
                "SonarCloud did not return all production files: "
                    + missing.size()
                    + " missing. First missing path: "
                    + missing.get(0)
            );
        }

        List<String> unexpectedParseErrors =
            parseErrorPaths.stream()
                .filter(
                    path ->
                        !expected.containsKey(path)
                )
                .sorted()
                .toList();

        if (!unexpectedParseErrors.isEmpty()) {
            throw new IOException(
                "Sonar parse error outside production inventory: "
                    + unexpectedParseErrors.get(0)
            );
        }

        List<SonarSmellMetric> result =
            new ArrayList<>();

        for (JavaFileInventoryEntry row : releaseRows) {
            String path = normalizePath(row.filePath());
            int nSmells = smellsByPath.get(path);

            if (nSmells < 0) {
                throw new IOException(
                    "Negative NSMELLS for " + path
                );
            }

            String analysisStatus =
                parseErrorPaths.contains(path)
                    ? "PARSE_ERROR"
                    : "OK";

            result.add(
                new SonarSmellMetric(
                    row.releaseIndex(),
                    row.version(),
                    row.commitId(),
                    row.filePath(),
                    nSmells,
                    analysisStatus
                )
            );
        }

        return List.copyOf(result);
    }

    private static String normalizePath(String value) {
        return value
            .replace('\\', '/')
            .replaceAll("^\\./+", "")
            .strip();
    }

    private static void addWorktree(
            Path repository,
            Path worktree,
            String commitId)
            throws IOException, InterruptedException {

        execute(
            repository,
            List.of(
                "git",
                "worktree",
                "add",
                "--detach",
                worktree.toString(),
                commitId
            )
        );
    }

    private static void removeWorktree(
            Path repository,
            Path worktree) {

        try {
            execute(
                repository,
                List.of(
                    "git",
                    "worktree",
                    "remove",
                    "--force",
                    worktree.toString()
                )
            );
        } catch (Exception exception) {
            System.err.printf(
                "Warning: unable to remove temporary "
                    + "worktree %s: %s%n",
                worktree,
                exception.getMessage()
            );
        }
    }

    private static void execute(
            Path workingDirectory,
            List<String> command)
            throws IOException, InterruptedException {

        Process process =
            new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .start();

        String output;

        try (InputStream stream = process.getInputStream()) {
            output = new String(
                stream.readAllBytes(),
                StandardCharsets.UTF_8
            );
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                "Command failed with exit code "
                    + exitCode
                    + ": "
                    + String.join(" ", command)
                    + System.lineSeparator()
                    + output.strip()
            );
        }
    }

    private static void deleteRecursively(Path root)
            throws IOException {

        if (root == null || !Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            List<Path> ordered =
                paths.sorted(
                    Comparator.reverseOrder()
                ).toList();

            for (Path path : ordered) {
                Files.deleteIfExists(path);
            }
        }
    }
    private record ScannerDiagnostics(
        Set<String> parseErrorPaths,
        Set<String> unresolvedMessages
    ) {
    }
}