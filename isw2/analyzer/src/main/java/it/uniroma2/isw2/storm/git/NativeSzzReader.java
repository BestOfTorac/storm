package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.inventory.JavaSourceClassifier;
import it.uniroma2.isw2.storm.model.SelectedFixCommit;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NativeSzzReader {

    private static final Pattern HUNK_PATTERN =
        Pattern.compile(
            "^@@ -(\\d+)(?:,(\\d+))? "
                + "\\+(\\d+)(?:,(\\d+))? @@.*$"
        );

    private static final Pattern BLAME_HEADER_PATTERN =
        Pattern.compile(
            "^([0-9a-fA-F]{40})\\s+"
                + "\\d+\\s+\\d+(?:\\s+\\d+)?$"
        );

    private final Path repositoryPath;

    private final Map<String, CommitAnalysis>
        analysisCache =
            new HashMap<>();

    private final Map<String, Set<String>>
        fixCommitsByIssue =
            new HashMap<>();

    private int diffReads = 0;
    private int blameReads = 0;
    private int fixCommitsWithoutDeletedProductionLines = 0;
    private int productionFilesWithDeletedLines = 0;
    private int sameIssueFixBlamesSkipped = 0;

    public NativeSzzReader(
            Path repositoryPath,
            List<SelectedFixCommit> allFixes) {

        this.repositoryPath =
            Objects.requireNonNull(
                repositoryPath,
                "Repository path cannot be null."
            )
            .toAbsolutePath()
            .normalize();

        Objects.requireNonNull(
            allFixes,
            "Fix catalog cannot be null."
        );

        for (SelectedFixCommit fix : allFixes) {

            fixCommitsByIssue
                .computeIfAbsent(
                    fix.issueKey(),
                    ignored ->
                        new HashSet<>()
                )
                .add(
                    fix.commitId()
                );
        }
    }

    public List<SzzBugIntroducingChange> analyze(
            SelectedFixCommit fix)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
            fix,
            "Fix cannot be null."
        );

        CommitAnalysis analysis =
            analysisCache.get(
                fix.commitId()
            );

        if (analysis == null) {

            analysis =
                analyzeCommit(
                    fix.commitId()
                );

            analysisCache.put(
                fix.commitId(),
                analysis
            );
        }

        Set<String> sameIssueFixCommits =
            fixCommitsByIssue.getOrDefault(
                fix.issueKey(),
                Set.of()
            );

        List<SzzBugIntroducingChange> output =
            new ArrayList<>();

        for (BlamedFileResult fileResult
                : analysis.files()) {

            for (
                Map.Entry<String, Integer> entry
                    : fileResult
                        .blamedLinesByCommit()
                        .entrySet()
            ) {
                String introducingCommit =
                    entry.getKey();

                /*
                 * A defect implemented in several commits can have a
                 * later fix commit modify lines introduced by an earlier
                 * fix commit of the same Jira issue.
                 *
                 * Such an earlier fix commit must not become a
                 * bug-introducing commit for the same defect.
                 */
                if (
                    sameIssueFixCommits.contains(
                        introducingCommit
                    )
                ) {
                    sameIssueFixBlamesSkipped++;
                    continue;
                }

                output.add(
                    new SzzBugIntroducingChange(
                        fix.issueKey(),
                        fix.commitId(),
                        fix.selectionStrategy(),
                        analysis.parentCommitId(),
                        fileResult.fixedFilePath(),
                        fileResult.blamedFilePath(),
                        introducingCommit,
                        entry.getValue()
                    )
                );
            }
        }

        output.sort(
            Comparator
                .comparing(
                    SzzBugIntroducingChange
                        ::fixedFilePath
                )
                .thenComparing(
                    SzzBugIntroducingChange
                        ::bugIntroducingCommitId
                )
        );

        return List.copyOf(
            output
        );
    }

    public int analyzedFixCommitCount() {
        return analysisCache.size();
    }

    public int diffReadCount() {
        return diffReads;
    }

    public int blameReadCount() {
        return blameReads;
    }

    public int fixCommitsWithoutDeletedProductionLinesCount() {
        return fixCommitsWithoutDeletedProductionLines;
    }

    public int productionFilesWithDeletedLinesCount() {
        return productionFilesWithDeletedLines;
    }

    public int sameIssueFixBlamesSkippedCount() {
        return sameIssueFixBlamesSkipped;
    }

    private CommitAnalysis analyzeCommit(
            String fixCommitId)
            throws IOException, InterruptedException {

        String parentCommitId =
            readFirstParent(
                fixCommitId
            );

        if (parentCommitId == null) {

            fixCommitsWithoutDeletedProductionLines++;

            return new CommitAnalysis(
                "",
                List.of()
            );
        }

        List<FileDeletionRanges> files =
            readDeletionRanges(
                parentCommitId,
                fixCommitId
            );

        if (files.isEmpty()) {
            fixCommitsWithoutDeletedProductionLines++;
        }

        List<BlamedFileResult> results =
            new ArrayList<>();

        for (FileDeletionRanges file : files) {

            productionFilesWithDeletedLines++;

            Map<String, Integer> blamedLines =
                new HashMap<>();

            for (LineRange range
                    : file.ranges()) {

                Map<String, Integer> rangeBlame =
                    blameRange(
                        parentCommitId,
                        file.blamedFilePath(),
                        range
                    );

                for (
                    Map.Entry<String, Integer> entry
                        : rangeBlame.entrySet()
                ) {
                    blamedLines.merge(
                        entry.getKey(),
                        entry.getValue(),
                        Integer::sum
                    );
                }
            }

            results.add(
                new BlamedFileResult(
                    file.fixedFilePath(),
                    file.blamedFilePath(),
                    Map.copyOf(
                        blamedLines
                    )
                )
            );
        }

        return new CommitAnalysis(
            parentCommitId,
            List.copyOf(results)
        );
    }

    private String readFirstParent(
            String commitId)
            throws IOException, InterruptedException {

        ProcessOutput output =
            execute(
                List.of(
                    "git",
                    "rev-list",
                    "--parents",
                    "-n",
                    "1",
                    commitId
                )
            );

        if (output.lines().isEmpty()) {
            return null;
        }

        String[] fields =
            output.lines()
                .get(0)
                .trim()
                .split("\\s+");

        if (fields.length < 2) {
            return null;
        }

        return fields[1];
    }

    private List<FileDeletionRanges> readDeletionRanges(
            String parentCommitId,
            String fixCommitId)
            throws IOException, InterruptedException {

        ProcessOutput output =
            execute(
                List.of(
                    "git",
                    "-c",
                    "diff.renameLimit=0",
                    "diff",
                    "--find-renames",
                    "--unified=0",
                    "--no-color",
                    "--no-ext-diff",
                    parentCommitId,
                    fixCommitId,
                    "--"
                )
            );

        diffReads++;

        List<FileDeletionRanges> files =
            new ArrayList<>();

        String currentOldPath = null;
        String currentNewPath = null;
        List<LineRange> currentRanges =
            new ArrayList<>();

        for (String line : output.lines()) {

            if (line.startsWith("diff --git ")) {

                addCurrentFile(
                    files,
                    currentOldPath,
                    currentNewPath,
                    currentRanges
                );

                currentOldPath = null;
                currentNewPath = null;
                currentRanges =
                    new ArrayList<>();

                continue;
            }

            if (line.startsWith("--- ")) {

                currentOldPath =
                    normalizePatchPath(
                        line.substring(4)
                    );

                continue;
            }

            if (line.startsWith("+++ ")) {

                currentNewPath =
                    normalizePatchPath(
                        line.substring(4)
                    );

                continue;
            }

            if (line.startsWith("@@ ")) {

                Matcher matcher =
                    HUNK_PATTERN.matcher(
                        line
                    );

                if (!matcher.matches()) {
                    throw new IOException(
                        "Unable to parse Git diff hunk: "
                            + line
                    );
                }

                int oldStart =
                    Integer.parseInt(
                        matcher.group(1)
                    );

                int oldCount =
                    matcher.group(2) == null
                        ? 1
                        : Integer.parseInt(
                            matcher.group(2)
                        );

                if (oldCount > 0) {

                    currentRanges.add(
                        new LineRange(
                            oldStart,
                            oldStart
                                + oldCount
                                - 1
                        )
                    );
                }
            }
        }

        addCurrentFile(
            files,
            currentOldPath,
            currentNewPath,
            currentRanges
        );

        return List.copyOf(
            files
        );
    }

    private static void addCurrentFile(
            List<FileDeletionRanges> files,
            String oldPath,
            String newPath,
            List<LineRange> ranges) {

        if (
            oldPath == null
                || ranges.isEmpty()
                || "/dev/null".equals(oldPath)
        ) {
            return;
        }

        String fixedPath =
            newPath == null
                    || "/dev/null".equals(newPath)
                ? oldPath
                : newPath;

        String classificationPath =
            !"/dev/null".equals(fixedPath)
                ? fixedPath
                : oldPath;

        if (
            JavaSourceClassifier
                .classify(classificationPath)
                != SourceCategory.PRODUCTION
        ) {
            return;
        }

        files.add(
            new FileDeletionRanges(
                fixedPath,
                oldPath,
                List.copyOf(ranges)
            )
        );
    }

    private Map<String, Integer> blameRange(
            String parentCommitId,
            String filePath,
            LineRange range)
            throws IOException, InterruptedException {

        ProcessOutput output =
            execute(
                List.of(
                    "git",
                    "blame",
                    "--line-porcelain",
                    "--root",
                    "-L",
                    range.start()
                        + ","
                        + range.end(),
                    parentCommitId,
                    "--",
                    filePath
                )
            );

        blameReads++;

        Map<String, Integer> blamed =
            new HashMap<>();

        for (String line : output.lines()) {

            Matcher matcher =
                BLAME_HEADER_PATTERN.matcher(
                    line
                );

            if (!matcher.matches()) {
                continue;
            }

            String commitId =
                matcher.group(1)
                    .toLowerCase();

            blamed.merge(
                commitId,
                1,
                Integer::sum
            );
        }

        if (blamed.isEmpty()) {
            throw new IOException(
                "Git blame returned no commits for "
                    + filePath
                    + " lines "
                    + range.start()
                    + "-"
                    + range.end()
                    + " at "
                    + parentCommitId
            );
        }

        return Map.copyOf(
            blamed
        );
    }

    private static String normalizePatchPath(
            String rawPath) {

        String path =
            rawPath.trim();

        if ("/dev/null".equals(path)) {
            return path;
        }

        if (
            path.length() >= 2
                && path.startsWith("\"")
                && path.endsWith("\"")
        ) {
            path =
                path.substring(
                    1,
                    path.length() - 1
                );
        }

        if (
            path.startsWith("a/")
                || path.startsWith("b/")
        ) {
            path =
                path.substring(2);
        }

        return path.replace(
            '\\',
            '/'
        );
    }

    private ProcessOutput execute(
            List<String> command)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
            new ProcessBuilder(
                command
            );

        processBuilder.directory(
            repositoryPath.toFile()
        );

        processBuilder
            .environment()
            .put(
                "LC_ALL",
                "C"
            );

        processBuilder.redirectErrorStream(
            true
        );

        Process process =
            processBuilder.start();

        List<String> lines =
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
                (line = reader.readLine()) != null
            ) {
                lines.add(line);
            }
        }

        int exitCode =
            process.waitFor();

        if (exitCode != 0) {

            throw new IOException(
                "Git command failed with exit code "
                    + exitCode
                    + ": "
                    + String.join(
                        " ",
                        command
                    )
            );
        }

        return new ProcessOutput(
            List.copyOf(lines)
        );
    }

    private record LineRange(
        int start,
        int end
    ) {
    }

    private record FileDeletionRanges(
        String fixedFilePath,
        String blamedFilePath,
        List<LineRange> ranges
    ) {
    }

    private record BlamedFileResult(
        String fixedFilePath,
        String blamedFilePath,
        Map<String, Integer> blamedLinesByCommit
    ) {
    }

    private record CommitAnalysis(
        String parentCommitId,
        List<BlamedFileResult> files
    ) {
    }

    private record ProcessOutput(
        List<String> lines
    ) {
    }
}