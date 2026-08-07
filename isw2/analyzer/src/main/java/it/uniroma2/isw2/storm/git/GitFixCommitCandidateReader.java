package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.inventory.JavaSourceClassifier;
import it.uniroma2.isw2.storm.model.FixCommitCandidate;
import it.uniroma2.isw2.storm.model.SourceCategory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitFixCommitCandidateReader {

    private static final Pattern ISSUE_PATTERN =
        Pattern.compile(
            "\\bSTORM-(\\d+)\\b",
            Pattern.CASE_INSENSITIVE
        );

    private static final String RECORD_SEPARATOR =
        "\u001e";

    private final Path repositoryPath;

    public GitFixCommitCandidateReader(
            Path repositoryPath) {

        this.repositoryPath =
            Objects.requireNonNull(
                repositoryPath,
                "Repository path cannot be null."
            )
            .toAbsolutePath()
            .normalize();
    }

    public List<FixCommitCandidate> read(
            List<String> releaseCommits,
            Set<String> eligibleIssueKeys)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
            releaseCommits,
            "Release commits cannot be null."
        );

        Objects.requireNonNull(
            eligibleIssueKeys,
            "Eligible issue keys cannot be null."
        );

        List<String> distinctReleaseCommits =
            releaseCommits.stream()
                .filter(
                    commit ->
                        commit != null
                            && !commit.isBlank()
                )
                .distinct()
                .toList();

        if (distinctReleaseCommits.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one release commit is required."
            );
        }

        /*
         * Git accepts multiple revision roots.
         *
         * The resulting log is the union of all commits reachable
         * from the official release commits. Commits reachable from
         * multiple releases are emitted only once.
         */
        List<String> command =
            new ArrayList<>();

        command.add("git");
        command.add("log");

        command.add(
            "--format="
                + "%H%x00"
                + "%P%x00"
                + "%aI%x00"
                + "%s%x00"
                + "%B%x00"
                + "%x1e"
        );

        command.addAll(
            distinctReleaseCommits
        );

        String output =
            executeForText(
                command
            );

        List<FixCommitCandidate> candidates =
            new ArrayList<>();

        String[] records =
            output.split(
                RECORD_SEPARATOR
            );

        for (String rawRecord : records) {

            String record =
                stripLeadingNewlines(
                    rawRecord
                );

            if (record.isBlank()) {
                continue;
            }

            String[] fields =
                record.split(
                    "\u0000",
                    -1
                );

            if (fields.length < 5) {
                throw new IOException(
                    "Unexpected git log record."
                );
            }

            String commitId =
                fields[0].trim();

            String parentField =
                fields[1].trim();

            OffsetDateTime commitDate =
                OffsetDateTime.parse(
                    fields[2].trim()
                );

            String subject =
                fields[3].trim();

            String body =
                fields[4];

            List<String> parents =
                parseParents(
                    parentField
                );

            Set<String> referencedIssues =
                extractEligibleIssues(
                    subject + "\n" + body,
                    eligibleIssueKeys
                );

            if (referencedIssues.isEmpty()) {
                continue;
            }

            Set<String> subjectIssues =
                extractEligibleIssues(
                    subject,
                    eligibleIssueKeys
                );

            ChangedFiles changedFiles =
                readChangedFiles(
                    commitId,
                    parents
                );

            boolean merge =
                parents.size() > 1;

            boolean revert =
                isRevert(
                    subject,
                    body
                );

            for (String issueKey
                    : referencedIssues) {

                candidates.add(
                    new FixCommitCandidate(
                        issueKey,
                        commitId,
                        commitDate,
                        parents.size(),
                        merge,
                        revert,
                        subjectIssues.contains(
                            issueKey
                        ),
                        changedFiles
                            .allFiles()
                            .size(),
                        changedFiles
                            .productionJavaFiles(),
                        subject
                    )
                );
            }
        }

        candidates.sort(
            Comparator
                .comparingInt(
                    (FixCommitCandidate candidate) ->
                        issueNumber(
                            candidate.issueKey()
                        )
                )
                .thenComparing(
                    FixCommitCandidate::commitDate
                )
                .thenComparing(
                    FixCommitCandidate::commitId
                )
        );

        return List.copyOf(
            candidates
        );
    }

    private ChangedFiles readChangedFiles(
            String commitId,
            List<String> parents)
            throws IOException, InterruptedException {

        List<String> command;

        if (parents.isEmpty()) {

            command =
                List.of(
                    "git",
                    "show",
                    "--format=",
                    "--name-only",
                    commitId
                );

        } else {

            /*
             * For merge commits this deliberately compares the merge
             * result with its first parent. This represents what the
             * merge introduced into the main line.
             *
             * For ordinary commits it is the standard parent-to-child
             * changeset.
             */
            command =
                List.of(
                    "git",
                    "diff",
                    "--name-only",
                    parents.get(0),
                    commitId
                );
        }

        String output =
            executeForText(
                command
            );

        Set<String> allFiles =
            new HashSet<>();

        Set<String> productionJavaFiles =
            new HashSet<>();

        for (String line
                : output.split("\\R")) {

            String filePath =
                line.trim()
                    .replace('\\', '/');

            if (filePath.isBlank()) {
                continue;
            }

            allFiles.add(
                filePath
            );

            if (
                JavaSourceClassifier
                    .classify(filePath)
                    == SourceCategory.PRODUCTION
            ) {
                productionJavaFiles.add(
                    filePath
                );
            }
        }

        List<String> sortedAllFiles =
            allFiles.stream()
                .sorted()
                .toList();

        List<String> sortedProductionFiles =
            productionJavaFiles.stream()
                .sorted()
                .toList();

        return new ChangedFiles(
            sortedAllFiles,
            sortedProductionFiles
        );
    }

    private static Set<String> extractEligibleIssues(
            String text,
            Set<String> eligibleIssueKeys) {

        Set<String> issues =
            new HashSet<>();

        Matcher matcher =
            ISSUE_PATTERN.matcher(
                text
            );

        while (matcher.find()) {

            String issueKey =
                (
                    "STORM-"
                        + matcher.group(1)
                )
                .toUpperCase(
                    Locale.ROOT
                );

            if (
                eligibleIssueKeys.contains(
                    issueKey
                )
            ) {
                issues.add(
                    issueKey
                );
            }
        }

        return Set.copyOf(
            issues
        );
    }

    private static boolean isRevert(
            String subject,
            String body) {

        String normalizedSubject =
            subject == null
                ? ""
                : subject
                    .trim()
                    .toLowerCase(
                        Locale.ROOT
                    );

        String normalizedBody =
            body == null
                ? ""
                : body
                    .toLowerCase(
                        Locale.ROOT
                    );

        return normalizedSubject
                .startsWith("revert ")
            || normalizedBody
                .contains(
                    "this reverts commit"
                );
    }

    private static List<String> parseParents(
            String parentField) {

        if (
            parentField == null
                || parentField.isBlank()
        ) {
            return List.of();
        }

        return List.of(
            parentField
                .trim()
                .split("\\s+")
        );
    }

    private static int issueNumber(
            String issueKey) {

        int separator =
            issueKey.lastIndexOf('-');

        return Integer.parseInt(
            issueKey.substring(
                separator + 1
            )
        );
    }

    private static String stripLeadingNewlines(
            String value) {

        int index = 0;

        while (
            index < value.length()
                && (
                    value.charAt(index) == '\r'
                        || value.charAt(index) == '\n'
                )
        ) {
            index++;
        }

        return value.substring(
            index
        );
    }

    private String executeForText(
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

        Process process =
            processBuilder.start();

        byte[] stdout =
            process
                .getInputStream()
                .readAllBytes();

        byte[] stderr =
            process
                .getErrorStream()
                .readAllBytes();

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
                    + System.lineSeparator()
                    + new String(
                        stderr,
                        StandardCharsets.UTF_8
                    )
            );
        }

        return new String(
            stdout,
            StandardCharsets.UTF_8
        );
    }

    private record ChangedFiles(
        List<String> allFiles,
        List<String> productionJavaFiles
    ) {
    }
}