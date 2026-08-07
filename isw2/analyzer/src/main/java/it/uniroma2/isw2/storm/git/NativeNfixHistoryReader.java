package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.model.FixCommitSelectionStrategy;
import it.uniroma2.isw2.storm.model.SelectedFixCommit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NativeNfixHistoryReader {

    private final Path repositoryPath;

    private final Map<String, Set<String>>
        nonMergeIssuesByCommit;

    private final Map<String, Set<String>>
        firstParentMergeIssuesByCommit;

    /*
     * For each dataset release we cache whether at least one selected
     * FIRST_PARENT_MERGE fix is actually reachable on its first-parent
     * line. If not, the second per-class Git query can be skipped.
     */
    private final Map<String, Boolean>
        reachableFirstParentFixCache =
            new HashMap<>();

    private int nonMergeHistoryReads = 0;
    private int firstParentHistoryReads = 0;

    public NativeNfixHistoryReader(
            Path repositoryPath,
            List<SelectedFixCommit> fixes) {

        this.repositoryPath =
            Objects.requireNonNull(
                repositoryPath,
                "Repository path cannot be null."
            )
            .toAbsolutePath()
            .normalize();

        Objects.requireNonNull(
            fixes,
            "Fix catalog cannot be null."
        );

        nonMergeIssuesByCommit =
            buildIssueMap(
                fixes,
                FixCommitSelectionStrategy
                    .NON_MERGE
            );

        firstParentMergeIssuesByCommit =
            buildIssueMap(
                fixes,
                FixCommitSelectionStrategy
                    .FIRST_PARENT_MERGE
            );
    }

    public NfixResult compute(
            String releaseCommit,
            String filePath)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
            releaseCommit,
            "Release commit cannot be null."
        );

        Objects.requireNonNull(
            filePath,
            "File path cannot be null."
        );

        Set<String> defectKeys =
            new HashSet<>();

        readMatchingIssues(
            List.of(
                "git",
                "-c",
                "diff.renameLimit=0",
                "log",
                releaseCommit,
                "--follow",
                "--no-merges",
                "--format=%H",
                "--",
                filePath
            ),
            nonMergeIssuesByCommit,
            defectKeys
        );

        nonMergeHistoryReads++;

        if (
            !firstParentMergeIssuesByCommit.isEmpty()
                && releaseHasReachableFirstParentFix(
                    releaseCommit
                )
        ) {
            readMatchingIssues(
                List.of(
                    "git",
                    "-c",
                    "diff.renameLimit=0",
                    "log",
                    releaseCommit,
                    "--follow",
                    "--first-parent",
                    "--format=%H",
                    "--",
                    filePath
                ),
                firstParentMergeIssuesByCommit,
                defectKeys
            );

            firstParentHistoryReads++;
        }

        return new NfixResult(
            defectKeys.size(),
            Set.copyOf(defectKeys)
        );
    }

    public int nonMergeHistoryReadCount() {
        return nonMergeHistoryReads;
    }

    public int firstParentHistoryReadCount() {
        return firstParentHistoryReads;
    }

    public long releasesWithReachableFirstParentFixCount() {

        return reachableFirstParentFixCache
            .values()
            .stream()
            .filter(Boolean::booleanValue)
            .count();
    }

    private boolean releaseHasReachableFirstParentFix(
            String releaseCommit)
            throws IOException, InterruptedException {

        Boolean cached =
            reachableFirstParentFixCache.get(
                releaseCommit
            );

        if (cached != null) {
            return cached;
        }

        ProcessOutput output =
            execute(
                List.of(
                    "git",
                    "rev-list",
                    "--first-parent",
                    releaseCommit
                )
            );

        boolean reachable = false;

        for (String line : output.lines()) {

            String commitId =
                line.trim();

            if (
                firstParentMergeIssuesByCommit
                    .containsKey(
                        commitId
                    )
            ) {
                reachable = true;
                break;
            }
        }

        reachableFirstParentFixCache.put(
            releaseCommit,
            reachable
        );

        return reachable;
    }

    private void readMatchingIssues(
            List<String> command,
            Map<String, Set<String>> issuesByCommit,
            Set<String> destination)
            throws IOException, InterruptedException {

        ProcessOutput output =
            execute(
                command
            );

        for (String line : output.lines()) {

            String commitId =
                line.trim();

            if (commitId.isBlank()) {
                continue;
            }

            Set<String> issues =
                issuesByCommit.get(
                    commitId
                );

            if (issues != null) {
                destination.addAll(
                    issues
                );
            }
        }
    }

    private static Map<String, Set<String>>
            buildIssueMap(
                List<SelectedFixCommit> fixes,
                FixCommitSelectionStrategy strategy) {

        Map<String, Set<String>> mutable =
            new HashMap<>();

        for (SelectedFixCommit fix : fixes) {

            if (
                fix.selectionStrategy()
                    != strategy
            ) {
                continue;
            }

            mutable
                .computeIfAbsent(
                    fix.commitId(),
                    ignored ->
                        new HashSet<>()
                )
                .add(
                    fix.issueKey()
                );
        }

        Map<String, Set<String>> result =
            new HashMap<>();

        for (
            Map.Entry<String, Set<String>> entry
                : mutable.entrySet()
        ) {
            result.put(
                entry.getKey(),
                Set.copyOf(
                    entry.getValue()
                )
            );
        }

        return Map.copyOf(
            result
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
                lines.add(
                    line
                );
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

    public record NfixResult(
        int nfix,
        Set<String> defectKeys
    ) {
        public NfixResult {
            defectKeys =
                Set.copyOf(
                    defectKeys
                );
        }
    }

    private record ProcessOutput(
        List<String> lines
    ) {
    }
}