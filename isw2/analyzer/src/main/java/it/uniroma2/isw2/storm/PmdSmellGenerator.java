package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.inventory.JavaInventoryCsvReader;
import it.uniroma2.isw2.storm.model.JavaFileInventoryEntry;
import it.uniroma2.isw2.storm.model.SourceCategory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PmdSmellGenerator {

    private static final String JAVA_VERSION =
        "java-1.8";

    private PmdSmellGenerator() {
        // Application entry point.
    }

    public static void main(String[] args) {

        int exitCode = run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {

        if (args.length != 2) {

            System.err.println(
                "Usage: PmdSmellGenerator "
                    + "<repository-path> "
                    + "<pmd-executable>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path pmdExecutable =
            Path.of(args[1])
                .toAbsolutePath()
                .normalize();

        Path inventoryPath =
            repositoryPath.resolve(
                "isw2/datasets/java_class_inventory.csv"
            );

        Path rulesetPath =
            repositoryPath.resolve(
                "isw2/config/pmd-smells.xml"
            );

        Path metricsOutput =
            repositoryPath.resolve(
                "isw2/datasets/smell_metrics.csv"
            );

        Path evidenceOutput =
            repositoryPath.resolve(
                "isw2/datasets/pmd_smell_evidence.csv"
            );

        Path temporaryRoot = null;

        try {

            if (!Files.isRegularFile(pmdExecutable)) {
                throw new IOException(
                    "PMD executable not found: "
                        + pmdExecutable
                );
            }

            if (!Files.isRegularFile(rulesetPath)) {
                throw new IOException(
                    "PMD ruleset not found: "
                        + rulesetPath
                );
            }

            List<JavaFileInventoryEntry> inventory =
                JavaInventoryCsvReader.read(
                    inventoryPath
                );

            List<JavaFileInventoryEntry> production =
                inventory.stream()
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
                byRelease =
                    new LinkedHashMap<>();

            for (JavaFileInventoryEntry row
                    : production) {

                byRelease
                    .computeIfAbsent(
                        row.releaseIndex(),
                        ignored ->
                            new ArrayList<>()
                    )
                    .add(row);
            }

            temporaryRoot =
                Files.createTempDirectory(
                    "storm-pmd-smells-"
                );

            List<SmellMetric> metrics =
                new ArrayList<>();

            List<SmellEvidence> evidence =
                new ArrayList<>();

            int releaseNumber = 0;

            for (
                Map.Entry<
                    Integer,
                    List<JavaFileInventoryEntry>
                > releaseEntry
                    : byRelease.entrySet()
            ) {

                releaseNumber++;

                int releaseIndex =
                    releaseEntry.getKey();

                List<JavaFileInventoryEntry>
                    releaseRows =
                        releaseEntry.getValue();

                JavaFileInventoryEntry representative =
                    releaseRows.get(0);

                String version =
                    representative.version();

                String commitId =
                    representative.commitId();

                validateReleaseRows(
                    releaseRows,
                    version,
                    commitId
                );

                System.out.printf(
                    "[%d/%d] %-18s classes=%d%n",
                    releaseNumber,
                    byRelease.size(),
                    version,
                    releaseRows.size()
                );

                Path worktree =
                    temporaryRoot.resolve(
                        "release-" + releaseIndex
                    );

                boolean worktreeCreated = false;

                try {

                    addWorktree(
                        repositoryPath,
                        worktree,
                        commitId
                    );

                    worktreeCreated = true;

                    Path fileList =
                        temporaryRoot.resolve(
                            "release-"
                                + releaseIndex
                                + "-files.txt"
                        );

                    writeFileList(
                        worktree,
                        releaseRows,
                        fileList
                    );

                    Path report =
                        temporaryRoot.resolve(
                            "release-"
                                + releaseIndex
                                + "-pmd.xml"
                        );

                    runPmd(
                        pmdExecutable,
                        rulesetPath,
                        fileList,
                        report
                    );

                    ReleaseAnalysis analysis =
                        readPmdReport(
                            report,
                            worktree,
                            releaseRows,
                            releaseIndex,
                            version,
                            commitId
                        );

                    metrics.addAll(
                        analysis.metrics()
                    );

                    evidence.addAll(
                        analysis.evidence()
                    );

                    System.out.printf(
                        "       violations=%d, "
                            + "classes-with-smells=%d, "
                            + "max=%d%n",
                        analysis.totalViolations(),
                        analysis.classesWithSmells(),
                        analysis.maximumSmells()
                    );

                } finally {

                    if (worktreeCreated) {

                        removeWorktree(
                            repositoryPath,
                            worktree
                        );
                    }
                }
            }

            metrics.sort(
                Comparator
                    .comparingInt(
                        SmellMetric::releaseIndex
                    )
                    .thenComparing(
                        SmellMetric::filePath
                    )
            );

            evidence.sort(
                Comparator
                    .comparingInt(
                        SmellEvidence::releaseIndex
                    )
                    .thenComparing(
                        SmellEvidence::filePath
                    )
                    .thenComparingInt(
                        SmellEvidence::line
                    )
                    .thenComparing(
                        SmellEvidence::rule
                    )
            );

            validateFinalResult(
                production,
                metrics,
                evidence
            );

            writeMetrics(
                metricsOutput,
                metrics
            );

            writeEvidence(
                evidenceOutput,
                evidence
            );

            long classesWithSmells =
                metrics.stream()
                    .filter(
                        row ->
                            row.nSmells() > 0
                    )
                    .count();

            int maximum =
                metrics.stream()
                    .mapToInt(
                        SmellMetric::nSmells
                    )
                    .max()
                    .orElse(0);

            long totalSmells =
                metrics.stream()
                    .mapToLong(
                        SmellMetric::nSmells
                    )
                    .sum();

            System.out.println();

            System.out.printf(
                "Production observations: %d%n",
                production.size()
            );

            System.out.printf(
                "Smell metric observations: %d%n",
                metrics.size()
            );

            System.out.printf(
                "Observations with NSMELLS > 0: %d%n",
                classesWithSmells
            );

            System.out.printf(
                "Total PMD smell violations: %d%n",
                totalSmells
            );

            System.out.printf(
                "Maximum NSMELLS: %d%n",
                maximum
            );

            System.out.printf(
                "PMD evidence rows: %d%n",
                evidence.size()
            );

            System.out.printf(
                "Smell metrics written to: %s%n",
                metricsOutput
            );

            System.out.printf(
                "PMD evidence written to: %s%n",
                evidenceOutput
            );

            return 0;

        } catch (Exception exception) {

            System.err.printf(
                "PMD smell generation failed: %s%n",
                exception.getMessage()
            );

            exception.printStackTrace(
                System.err
            );

            return 1;

        } finally {

            if (temporaryRoot != null) {

                try {
                    deleteRecursively(
                        temporaryRoot
                    );
                } catch (IOException ignored) {
                    // Best-effort cleanup.
                }
            }
        }
    }

    private static void validateReleaseRows(
            List<JavaFileInventoryEntry> rows,
            String expectedVersion,
            String expectedCommit)
            throws IOException {

        for (JavaFileInventoryEntry row
                : rows) {

            if (
                !expectedVersion.equals(
                    row.version()
                )
            ) {
                throw new IOException(
                    "Multiple versions found for release "
                        + row.releaseIndex()
                );
            }

            if (
                !expectedCommit.equals(
                    row.commitId()
                )
            ) {
                throw new IOException(
                    "Multiple commits found for release "
                        + row.releaseIndex()
                );
            }
        }
    }

    private static void addWorktree(
            Path repository,
            Path worktree,
            String commitId)
            throws IOException,
            InterruptedException {

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
                "Warning: unable to remove "
                    + "temporary worktree %s: %s%n",
                worktree,
                exception.getMessage()
            );
        }
    }

    private static void writeFileList(
            Path worktree,
            List<JavaFileInventoryEntry> rows,
            Path fileList)
            throws IOException {

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    fileList,
                    StandardCharsets.UTF_8
                )
        ) {

            for (JavaFileInventoryEntry row
                    : rows) {

                Path source =
                    worktree
                        .resolve(
                            row.filePath()
                        )
                        .normalize();

                if (!Files.isRegularFile(source)) {

                    throw new IOException(
                        "Production source missing "
                            + "from worktree: "
                            + row.filePath()
                    );
                }

                writer.write(
                    source
                        .toAbsolutePath()
                        .normalize()
                        .toString()
                );

                writer.newLine();
            }
        }
    }

    private static void runPmd(
            Path pmdExecutable,
            Path ruleset,
            Path fileList,
            Path report)
            throws IOException,
            InterruptedException {

        List<String> arguments =
            List.of(
                "check",
                "--file-list",
                fileList.toString(),
                "-R",
                ruleset.toString(),
                "-f",
                "xml",
                "-r",
                report.toString(),
                "--use-version",
                JAVA_VERSION,
                "--no-fail-on-violation",
                "--no-progress",
                "--no-cache",
                "--threads",
                "0"
            );

        List<String> command =
            new ArrayList<>();

        String executableName =
            pmdExecutable
                .getFileName()
                .toString()
                .toLowerCase();

        if (
            executableName.endsWith(".bat")
                || executableName.endsWith(".cmd")
        ) {

            command.add("cmd.exe");
            command.add("/d");
            command.add("/c");
        }

        command.add(
            pmdExecutable.toString()
        );

        command.addAll(arguments);

        execute(
            pmdExecutable.getParent(),
            command
        );
    }

    private static ReleaseAnalysis readPmdReport(
            Path report,
            Path worktree,
            List<JavaFileInventoryEntry> releaseRows,
            int releaseIndex,
            String version,
            String commitId)
            throws Exception {

        DocumentBuilderFactory factory =
            DocumentBuilderFactory
                .newInstance();

        factory.setNamespaceAware(true);

        factory.setFeature(
            "http://apache.org/xml/features/"
                + "disallow-doctype-decl",
            true
        );

        Document document =
            factory
                .newDocumentBuilder()
                .parse(
                    report.toFile()
                );

        NodeList errors =
            document.getElementsByTagNameNS(
                "*",
                "error"
            );

        if (errors.getLength() > 0) {

            Element first =
                (Element) errors.item(0);

            throw new IOException(
                "PMD reported "
                    + errors.getLength()
                    + " processing error(s). First: "
                    + first.getAttribute("filename")
                    + " - "
                    + first.getAttribute("msg")
            );
        }

        NodeList configErrors =
            document.getElementsByTagNameNS(
                "*",
                "configerror"
            );

        if (configErrors.getLength() > 0) {

            throw new IOException(
                "PMD reported "
                    + configErrors.getLength()
                    + " configuration error(s)."
            );
        }

        Map<String, Integer> counts =
            new HashMap<>();

        Map<String, JavaFileInventoryEntry>
            inventoryByPath =
                new HashMap<>();

        for (JavaFileInventoryEntry row
                : releaseRows) {

            counts.put(
                row.filePath(),
                0
            );

            inventoryByPath.put(
                row.filePath(),
                row
            );
        }

        List<SmellEvidence> evidence =
            new ArrayList<>();

        NodeList files =
            document.getElementsByTagNameNS(
                "*",
                "file"
            );

        for (
            int fileIndex = 0;
            fileIndex < files.getLength();
            fileIndex++
        ) {

            Element fileElement =
                (Element) files.item(
                    fileIndex
                );

            String reportedName =
                fileElement.getAttribute(
                    "name"
                );

            Path reportedPath =
                Path.of(reportedName)
                    .toAbsolutePath()
                    .normalize();

            Path normalizedWorktree =
                worktree
                    .toAbsolutePath()
                    .normalize();

            if (
                !reportedPath.startsWith(
                    normalizedWorktree
                )
            ) {
                throw new IOException(
                    "PMD reported a file outside "
                        + "the temporary worktree: "
                        + reportedPath
                );
            }

            String relativePath =
                normalizePath(
                    normalizedWorktree
                        .relativize(
                            reportedPath
                        )
                        .toString()
                );

            JavaFileInventoryEntry inventoryRow =
                inventoryByPath.get(
                    relativePath
                );

            if (inventoryRow == null) {

                throw new IOException(
                    "PMD reported a file outside "
                        + "the production inventory: "
                        + relativePath
                );
            }

            NodeList children =
                fileElement
                    .getChildNodes();

            for (
                int childIndex = 0;
                childIndex < children.getLength();
                childIndex++
            ) {

                Node child =
                    children.item(
                        childIndex
                    );

                if (
                    child.getNodeType()
                        != Node.ELEMENT_NODE
                ) {
                    continue;
                }

                Element element =
                    (Element) child;

                if (
                    !"violation".equals(
                        element.getLocalName()
                    )
                ) {
                    continue;
                }

                counts.merge(
                    relativePath,
                    1,
                    Integer::sum
                );

                evidence.add(
                    new SmellEvidence(
                        releaseIndex,
                        version,
                        commitId,
                        relativePath,
                        element.getAttribute(
                            "ruleset"
                        ),
                        element.getAttribute(
                            "rule"
                        ),
                        parseInteger(
                            element.getAttribute(
                                "priority"
                            ),
                            "priority"
                        ),
                        parseInteger(
                            element.getAttribute(
                                "beginline"
                            ),
                            "beginline"
                        ),
                        element
                            .getTextContent()
                            .trim()
                            .replaceAll(
                                "\\s+",
                                " "
                            )
                    )
                );
            }
        }

        List<SmellMetric> metrics =
            new ArrayList<>();

        int classesWithSmells = 0;
        int maximum = 0;
        int total = 0;

        for (JavaFileInventoryEntry row
                : releaseRows) {

            int nSmells =
                counts.getOrDefault(
                    row.filePath(),
                    0
                );

            if (nSmells > 0) {
                classesWithSmells++;
            }

            maximum =
                Math.max(
                    maximum,
                    nSmells
                );

            total += nSmells;

            metrics.add(
                new SmellMetric(
                    row.releaseIndex(),
                    row.version(),
                    row.commitId(),
                    row.filePath(),
                    nSmells
                )
            );
        }

        if (total != evidence.size()) {

            throw new IOException(
                "PMD smell accounting mismatch "
                    + "for release "
                    + releaseIndex
            );
        }

        return new ReleaseAnalysis(
            List.copyOf(metrics),
            List.copyOf(evidence),
            total,
            classesWithSmells,
            maximum
        );
    }

    private static void validateFinalResult(
            List<JavaFileInventoryEntry> production,
            List<SmellMetric> metrics,
            List<SmellEvidence> evidence)
            throws IOException {

        if (
            production.size()
                != metrics.size()
        ) {

            throw new IOException(
                "Smell metrics do not match "
                    + "production inventory size: "
                    + metrics.size()
                    + " != "
                    + production.size()
            );
        }

        Map<String, SmellMetric> metricByKey =
            new HashMap<>();

        long totalSmells = 0;

        for (SmellMetric row : metrics) {

            if (row.nSmells() < 0) {
                throw new IOException(
                    "Negative NSMELLS."
                );
            }

            String key =
                key(
                    row.releaseIndex(),
                    row.filePath()
                );

            if (
                metricByKey.put(
                    key,
                    row
                ) != null
            ) {

                throw new IOException(
                    "Duplicate smell metric: "
                        + key
                );
            }

            totalSmells +=
                row.nSmells();
        }

        if (
            totalSmells
                != evidence.size()
        ) {

            throw new IOException(
                "Final PMD evidence/count mismatch."
            );
        }

        for (SmellEvidence row
                : evidence) {

            String key =
                key(
                    row.releaseIndex(),
                    row.filePath()
                );

            if (
                !metricByKey.containsKey(key)
            ) {

                throw new IOException(
                    "PMD evidence without "
                        + "smell metric: "
                        + key
                );
            }
        }
    }

    private static void writeMetrics(
            Path output,
            List<SmellMetric> rows)
            throws IOException {

        Files.createDirectories(
            output.getParent()
        );

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    output,
                    StandardCharsets.UTF_8
                )
        ) {

            writer.write(
                "ReleaseIndex,Version,CommitId,"
                    + "FilePath,NSMELLS"
            );

            writer.newLine();

            for (SmellMetric row : rows) {

                writer.write(
                    Integer.toString(
                        row.releaseIndex()
                    )
                );

                writer.write(',');
                writer.write(
                    csv(row.version())
                );

                writer.write(',');
                writer.write(
                    csv(row.commitId())
                );

                writer.write(',');
                writer.write(
                    csv(row.filePath())
                );

                writer.write(',');
                writer.write(
                    Integer.toString(
                        row.nSmells()
                    )
                );

                writer.newLine();
            }
        }
    }

    private static void writeEvidence(
            Path output,
            List<SmellEvidence> rows)
            throws IOException {

        Files.createDirectories(
            output.getParent()
        );

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    output,
                    StandardCharsets.UTF_8
                )
        ) {

            writer.write(
                "ReleaseIndex,Version,CommitId,"
                    + "FilePath,RuleSet,Rule,"
                    + "Priority,Line,Description"
            );

            writer.newLine();

            for (SmellEvidence row
                    : rows) {

                writer.write(
                    Integer.toString(
                        row.releaseIndex()
                    )
                );

                writer.write(',');
                writer.write(
                    csv(row.version())
                );

                writer.write(',');
                writer.write(
                    csv(row.commitId())
                );

                writer.write(',');
                writer.write(
                    csv(row.filePath())
                );

                writer.write(',');
                writer.write(
                    csv(row.ruleSet())
                );

                writer.write(',');
                writer.write(
                    csv(row.rule())
                );

                writer.write(',');
                writer.write(
                    Integer.toString(
                        row.priority()
                    )
                );

                writer.write(',');
                writer.write(
                    Integer.toString(
                        row.line()
                    )
                );

                writer.write(',');
                writer.write(
                    csv(row.description())
                );

                writer.newLine();
            }
        }
    }

    private static void execute(
            Path directory,
            List<String> command)
            throws IOException,
            InterruptedException {

        ProcessBuilder processBuilder =
            new ProcessBuilder(command);

        processBuilder.directory(
            directory.toFile()
        );

        processBuilder.redirectErrorStream(
            true
        );

        Process process =
            processBuilder.start();

        List<String> output =
            new ArrayList<>();

        try (
            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
        ) {

            String line;

            while (
                (line = reader.readLine())
                    != null
            ) {

                output.add(line);
            }
        }

        int exitCode =
            process.waitFor();

        if (exitCode != 0) {

            throw new IOException(
                "Command failed with exit code "
                    + exitCode
                    + System.lineSeparator()
                    + String.join(
                        System.lineSeparator(),
                        output
                    )
            );
        }
    }

    private static void deleteRecursively(
            Path path)
            throws IOException {

        if (!Files.exists(path)) {
            return;
        }

        try (
            var paths =
                Files.walk(path)
        ) {

            paths
                .sorted(
                    Comparator.reverseOrder()
                )
                .forEach(
                    current -> {

                        try {
                            Files.deleteIfExists(
                                current
                            );
                        } catch (
                                IOException exception
                        ) {
                            throw new RuntimeException(
                                exception
                            );
                        }
                    }
                );
        } catch (
                RuntimeException exception
        ) {

            if (
                exception.getCause()
                    instanceof IOException ioException
            ) {
                throw ioException;
            }

            throw exception;
        }
    }

    private static String normalizePath(
            String path) {

        return path.replace(
            '\\',
            '/'
        );
    }

    private static String key(
            int releaseIndex,
            String filePath) {

        return releaseIndex
            + "|"
            + filePath;
    }

    private static int parseInteger(
            String value,
            String field)
            throws IOException {

        try {
            return Integer.parseInt(
                value
            );

        } catch (
                NumberFormatException exception
        ) {

            throw new IOException(
                "Invalid PMD "
                    + field
                    + ": "
                    + value,
                exception
            );
        }
    }

    private static String csv(
            String value) {

        if (value == null) {
            return "";
        }

        if (
            !value.contains(",")
                && !value.contains("\"")
                && !value.contains("\n")
                && !value.contains("\r")
        ) {
            return value;
        }

        return "\""
            + value.replace(
                "\"",
                "\"\""
            )
            + "\"";
    }

    private record SmellMetric(
        int releaseIndex,
        String version,
        String commitId,
        String filePath,
        int nSmells
    ) {
    }

    private record SmellEvidence(
        int releaseIndex,
        String version,
        String commitId,
        String filePath,
        String ruleSet,
        String rule,
        int priority,
        int line,
        String description
    ) {
    }

    private record ReleaseAnalysis(
        List<SmellMetric> metrics,
        List<SmellEvidence> evidence,
        int totalViolations,
        int classesWithSmells,
        int maximumSmells
    ) {
    }
}