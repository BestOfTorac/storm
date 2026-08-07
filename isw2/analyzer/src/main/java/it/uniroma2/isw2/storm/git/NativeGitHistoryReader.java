package it.uniroma2.isw2.storm.git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NativeGitHistoryReader {

    private static final String RECORD_PREFIX =
        "@@ISW2@@";

    private final Path repositoryPath;

    /*
     * Commit-level statistics are shared by every class touched by the
     * same commit, so they are computed once and cached.
     */
    private final Map<String, CommitStats>
        commitStatsCache =
            new HashMap<>();

    /*
     * Cache the non-merge revisions that belong exclusively to merged
     * parents of a candidate introduction merge.
     *
     * Key: merge commit + historical path.
     */
    private final Map<String, Set<String>>
        mergedBranchRevisionCache =
            new HashMap<>();

    private int recoveredMergeIntroductions = 0;
    private int skippedDuplicateMergeIntroductions = 0;

    public NativeGitHistoryReader(
            Path repositoryPath) {

        this.repositoryPath =
            Objects.requireNonNull(
                repositoryPath,
                "Repository path cannot be null."
            )
            .toAbsolutePath()
            .normalize();
    }

    public List<NativeGitRevision> readHistory(
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

        List<RawRevision> rawRevisions =
            readNormalHistory(
                releaseCommit,
                filePath
            );

        List<NativeGitRevision> revisions =
            new ArrayList<>(
                rawRevisions.size() + 1
            );

        Set<String> normalCommitIds =
            new HashSet<>();

        for (RawRevision rawRevision
                : rawRevisions) {

            CommitStats commitStats =
                readCommitStats(
                    rawRevision.commitId()
                );

            revisions.add(
                new NativeGitRevision(
                    rawRevision.commitId(),
                    rawRevision.commitDate(),
                    rawRevision.authorEmail(),
                    rawRevision.added(),
                    rawRevision.deleted(),
                    commitStats.changeSetSize(),
                    commitStats.nd(),
                    commitStats.entropy(),
                    false
                )
            );

            normalCommitIds.add(
                rawRevision.commitId()
            );
        }

        FirstParentRevision introduction =
            readOldestFirstParentRevision(
                releaseCommit,
                filePath
            );

        if (
            introduction != null
                && introduction.isMerge()
                && introduction.firstParentId() != null
                && introduction.historicalPath() != null
                && isAddedComparedToFirstParent(
                    introduction
                )
        ) {
            boolean alreadyRepresented =
                mergeIntroductionAlreadyRepresented(
                    introduction,
                    normalCommitIds
                );

            if (alreadyRepresented) {
                skippedDuplicateMergeIntroductions++;

            } else {
                CommitStats commitStats =
                    readFirstParentCommitStats(
                        introduction.commitId(),
                        introduction.firstParentId()
                    );

                revisions.add(
                    new NativeGitRevision(
                        introduction.commitId(),
                        introduction.commitDate(),
                        introduction.authorEmail(),
                        introduction.added(),
                        introduction.deleted(),
                        commitStats.changeSetSize(),
                        commitStats.nd(),
                        commitStats.entropy(),
                        true
                    )
                );

                recoveredMergeIntroductions++;
            }
        }

        return List.copyOf(
            revisions
        );
    }

    public int cachedChangeSetCount() {
        return commitStatsCache.size();
    }

    public int cachedCommitStatsCount() {
        return commitStatsCache.size();
    }

    public int recoveredMergeIntroductionCount() {
        return recoveredMergeIntroductions;
    }

    public int skippedDuplicateMergeIntroductionCount() {
        return skippedDuplicateMergeIntroductions;
    }

    private List<RawRevision> readNormalHistory(
            String releaseCommit,
            String filePath)
            throws IOException, InterruptedException {

        List<String> command =
            List.of(
                "git",
                "-c",
                "diff.renameLimit=0",
                "log",
                releaseCommit,
                "--follow",
                "--no-merges",
                "--date=short",
                "--format="
                    + RECORD_PREFIX
                    + "%H|%ad|%ae",
                "--numstat",
                "--",
                filePath
            );

        ProcessOutput output =
            execute(command);

        return parseNormalHistory(
            output.lines()
        );
    }

    private FirstParentRevision
            readOldestFirstParentRevision(
                String releaseCommit,
                String filePath)
            throws IOException, InterruptedException {

        List<String> command =
            List.of(
                "git",
                "-c",
                "diff.renameLimit=0",
                "log",
                releaseCommit,
                "--follow",
                "--first-parent",
                "--reverse",
                "--date=short",
                "--format="
                    + RECORD_PREFIX
                    + "%H|%P|%ad|%ae",
                "--numstat",
                "--",
                filePath
            );

        ProcessOutput output =
            execute(command);

        List<FirstParentRevision> revisions =
            parseFirstParentHistory(
                output.lines()
            );

        if (revisions.isEmpty()) {
            return null;
        }

        /*
         * --reverse makes the first record the oldest revision that
         * affects the followed file on the first-parent line.
         */
        return revisions.get(0);
    }

    private boolean isAddedComparedToFirstParent(
            FirstParentRevision revision)
            throws IOException, InterruptedException {

        List<String> command =
            List.of(
                "git",
                "-c",
                "diff.renameLimit=0",
                "diff",
                "--name-status",
                "--find-renames",
                revision.firstParentId(),
                revision.commitId(),
                "--",
                revision.historicalPath()
            );

        ProcessOutput output =
            execute(command);

        for (String line : output.lines()) {

            if (line.isBlank()) {
                continue;
            }

            String[] columns =
                line.split(
                    "\t",
                    3
                );

            if (columns.length == 0) {
                continue;
            }

            return "A".equals(
                columns[0].trim()
            );
        }

        return false;
    }

    private boolean mergeIntroductionAlreadyRepresented(
            FirstParentRevision introduction,
            Set<String> normalCommitIds)
            throws IOException, InterruptedException {

        if (normalCommitIds.isEmpty()) {
            return false;
        }

        Set<String> branchRevisionIds =
            readMergedBranchRevisionIds(
                introduction
            );

        for (String branchCommit
                : branchRevisionIds) {

            if (
                normalCommitIds.contains(
                    branchCommit
                )
            ) {
                return true;
            }
        }

        return false;
    }

    private Set<String> readMergedBranchRevisionIds(
            FirstParentRevision introduction)
            throws IOException, InterruptedException {

        String cacheKey =
            introduction.commitId()
                + "\u0000"
                + introduction.historicalPath();

        Set<String> cached =
            mergedBranchRevisionCache.get(
                cacheKey
            );

        if (cached != null) {
            return cached;
        }

        Set<String> commitIds =
            new HashSet<>();

        List<String> parents =
            introduction.parentIds();

        if (parents.size() <= 1) {
            return Set.of();
        }

        String firstParent =
            parents.get(0);

        for (int i = 1; i < parents.size(); i++) {

            String mergedParent =
                parents.get(i);

            List<String> command =
                List.of(
                    "git",
                    "log",
                    mergedParent,
                    "^" + firstParent,
                    "--follow",
                    "--no-merges",
                    "--format=%H",
                    "--",
                    introduction.historicalPath()
                );

            ProcessOutput output =
                execute(command);

            for (String line
                    : output.lines()) {

                String commitId =
                    line.trim();

                if (!commitId.isBlank()) {
                    commitIds.add(
                        commitId
                    );
                }
            }
        }

        Set<String> result =
            Set.copyOf(
                commitIds
            );

        mergedBranchRevisionCache.put(
            cacheKey,
            result
        );

        return result;
    }

    private List<RawRevision> parseNormalHistory(
            List<String> lines)
            throws IOException {

        List<RawRevision> revisions =
            new ArrayList<>();

        String currentCommit = null;
        LocalDate currentDate = null;
        String currentAuthor = null;

        int currentAdded = 0;
        int currentDeleted = 0;

        for (String line : lines) {

            if (line.startsWith(RECORD_PREFIX)) {

                if (currentCommit != null) {
                    revisions.add(
                        new RawRevision(
                            currentCommit,
                            currentDate,
                            currentAuthor,
                            currentAdded,
                            currentDeleted
                        )
                    );
                }

                String metadata =
                    line.substring(
                        RECORD_PREFIX.length()
                    );

                String[] fields =
                    metadata.split(
                        "\\|",
                        3
                    );

                if (fields.length != 3) {
                    throw new IOException(
                        "Unexpected git log metadata: "
                            + line
                    );
                }

                currentCommit =
                    fields[0];

                currentDate =
                    LocalDate.parse(
                        fields[1]
                    );

                currentAuthor =
                    normalizeAuthor(
                        fields[2]
                    );

                currentAdded = 0;
                currentDeleted = 0;

                continue;
            }

            if (
                currentCommit == null
                    || line.isBlank()
            ) {
                continue;
            }

            NumStatEntry entry =
                parseNumStatLine(
                    line
                );

            if (
                entry != null
                    && entry.numeric()
            ) {
                currentAdded +=
                    entry.added();

                currentDeleted +=
                    entry.deleted();
            }
        }

        if (currentCommit != null) {
            revisions.add(
                new RawRevision(
                    currentCommit,
                    currentDate,
                    currentAuthor,
                    currentAdded,
                    currentDeleted
                )
            );
        }

        return revisions;
    }

    private List<FirstParentRevision>
            parseFirstParentHistory(
                List<String> lines)
            throws IOException {

        List<FirstParentRevision> revisions =
            new ArrayList<>();

        String currentCommit = null;
        String currentParents = null;
        LocalDate currentDate = null;
        String currentAuthor = null;

        int currentAdded = 0;
        int currentDeleted = 0;
        String currentHistoricalPath = null;

        for (String line : lines) {

            if (line.startsWith(RECORD_PREFIX)) {

                if (currentCommit != null) {
                    revisions.add(
                        createFirstParentRevision(
                            currentCommit,
                            currentParents,
                            currentDate,
                            currentAuthor,
                            currentAdded,
                            currentDeleted,
                            currentHistoricalPath
                        )
                    );
                }

                String metadata =
                    line.substring(
                        RECORD_PREFIX.length()
                    );

                String[] fields =
                    metadata.split(
                        "\\|",
                        4
                    );

                if (fields.length != 4) {
                    throw new IOException(
                        "Unexpected first-parent metadata: "
                            + line
                    );
                }

                currentCommit =
                    fields[0];

                currentParents =
                    fields[1].trim();

                currentDate =
                    LocalDate.parse(
                        fields[2]
                    );

                currentAuthor =
                    normalizeAuthor(
                        fields[3]
                    );

                currentAdded = 0;
                currentDeleted = 0;
                currentHistoricalPath = null;

                continue;
            }

            if (
                currentCommit == null
                    || line.isBlank()
            ) {
                continue;
            }

            NumStatEntry entry =
                parseNumStatLine(
                    line
                );

            if (entry == null) {
                continue;
            }

            if (currentHistoricalPath == null) {
                currentHistoricalPath =
                    effectivePath(
                        entry.path()
                    );
            }

            if (entry.numeric()) {
                currentAdded +=
                    entry.added();

                currentDeleted +=
                    entry.deleted();
            }
        }

        if (currentCommit != null) {
            revisions.add(
                createFirstParentRevision(
                    currentCommit,
                    currentParents,
                    currentDate,
                    currentAuthor,
                    currentAdded,
                    currentDeleted,
                    currentHistoricalPath
                )
            );
        }

        return revisions;
    }

    private static FirstParentRevision
            createFirstParentRevision(
                String commitId,
                String parentField,
                LocalDate commitDate,
                String authorEmail,
                int added,
                int deleted,
                String historicalPath) {

        List<String> parents =
            new ArrayList<>();

        if (
            parentField != null
                && !parentField.isBlank()
        ) {
            for (String parent
                    : parentField
                        .trim()
                        .split("\\s+")) {

                if (!parent.isBlank()) {
                    parents.add(
                        parent
                    );
                }
            }
        }

        boolean merge =
            parents.size() > 1;

        String firstParentId =
            parents.isEmpty()
                ? null
                : parents.get(0);

        return new FirstParentRevision(
            commitId,
            commitDate,
            authorEmail,
            added,
            deleted,
            merge,
            firstParentId,
            List.copyOf(parents),
            historicalPath
        );
    }

    private CommitStats readCommitStats(
            String commitId)
            throws IOException, InterruptedException {

        CommitStats cached =
            commitStatsCache.get(
                commitId
            );

        if (cached != null) {
            return cached;
        }

        List<String> command =
            List.of(
                "git",
                "-c",
                "diff.renameLimit=0",
                "show",
                "--format=",
                "--numstat",
                "--find-renames",
                commitId
            );

        ProcessOutput output =
            execute(command);

        CommitStats stats =
            computeCommitStats(
                output.lines()
            );

        commitStatsCache.put(
            commitId,
            stats
        );

        return stats;
    }

    private CommitStats readFirstParentCommitStats(
            String commitId,
            String firstParentId)
            throws IOException, InterruptedException {

        CommitStats cached =
            commitStatsCache.get(
                commitId
            );

        if (cached != null) {
            return cached;
        }

        List<String> command =
            List.of(
                "git",
                "-c",
                "diff.renameLimit=0",
                "diff",
                "--numstat",
                "--find-renames",
                firstParentId,
                commitId
            );

        ProcessOutput output =
            execute(command);

        CommitStats stats =
            computeCommitStats(
                output.lines()
            );

        commitStatsCache.put(
            commitId,
            stats
        );

        return stats;
    }

    private static CommitStats computeCommitStats(
            List<String> lines)
            throws IOException {

        int changeSetSize = 0;

        Set<String> directories =
            new HashSet<>();

        List<Long> fileChanges =
            new ArrayList<>();

        long totalChanged = 0;

        for (String line : lines) {

            if (line.isBlank()) {
                continue;
            }

            NumStatEntry entry =
                parseNumStatLine(
                    line
                );

            if (entry == null) {
                continue;
            }

            changeSetSize++;

            String path =
                effectivePath(
                    entry.path()
                );

            directories.add(
                parentDirectory(
                    path
                )
            );

            if (!entry.numeric()) {
                continue;
            }

            long changed =
                (long) entry.added()
                    + entry.deleted();

            if (changed > 0) {
                fileChanges.add(
                    changed
                );

                totalChanged +=
                    changed;
            }
        }

        double entropy =
            normalizedEntropy(
                fileChanges,
                totalChanged
            );

        return new CommitStats(
            changeSetSize,
            directories.size(),
            entropy
        );
    }

    private static double normalizedEntropy(
            List<Long> fileChanges,
            long totalChanged) {

        int n =
            fileChanges.size();

        if (
            n <= 1
                || totalChanged <= 0
        ) {
            return 0.0;
        }

        double entropy = 0.0;

        for (long changed : fileChanges) {

            double probability =
                (double) changed
                    / totalChanged;

            entropy -=
                probability
                    * (
                        Math.log(probability)
                            / Math.log(2.0)
                    );
        }

        double maximumEntropy =
            Math.log(n)
                / Math.log(2.0);

        if (maximumEntropy <= 0.0) {
            return 0.0;
        }

        double normalized =
            entropy
                / maximumEntropy;

        /*
         * Protect against tiny floating-point errors.
         */
        return Math.max(
            0.0,
            Math.min(
                1.0,
                normalized
            )
        );
    }

    private static NumStatEntry parseNumStatLine(
            String line)
            throws IOException {

        String[] columns =
            line.split(
                "\t",
                3
            );

        if (columns.length < 3) {
            return null;
        }

        String path =
            columns[2];

        if (
            "-".equals(columns[0])
                || "-".equals(columns[1])
        ) {
            return new NumStatEntry(
                0,
                0,
                path,
                false
            );
        }

        try {
            return new NumStatEntry(
                Integer.parseInt(
                    columns[0]
                ),
                Integer.parseInt(
                    columns[1]
                ),
                path,
                true
            );

        } catch (NumberFormatException exception) {
            throw new IOException(
                "Unexpected git numstat line: "
                    + line,
                exception
            );
        }
    }

    private static String effectivePath(
            String path) {

        if (path == null) {
            return "";
        }

        String normalized =
            path.replace(
                '\\',
                '/'
            );

        int openBrace =
            normalized.indexOf('{');

        int arrow =
            normalized.indexOf(
                " => "
            );

        int closeBrace =
            normalized.indexOf('}');

        /*
         * Git compact rename notation:
         *
         * src/{old => new}/File.java
         */
        if (
            openBrace >= 0
                && arrow > openBrace
                && closeBrace > arrow
        ) {
            String prefix =
                normalized.substring(
                    0,
                    openBrace
                );

            String replacement =
                normalized.substring(
                    arrow + 4,
                    closeBrace
                );

            String suffix =
                normalized.substring(
                    closeBrace + 1
                );

            return prefix
                + replacement
                + suffix;
        }

        /*
         * Git non-compact rename notation:
         *
         * old/path/File.java => new/path/File.java
         */
        if (arrow >= 0) {
            return normalized
                .substring(
                    arrow + 4
                )
                .trim();
        }

        return normalized;
    }

    private static String parentDirectory(
            String path) {

        if (
            path == null
                || path.isBlank()
        ) {
            return ".";
        }

        int lastSlash =
            path.lastIndexOf('/');

        if (lastSlash < 0) {
            return ".";
        }

        if (lastSlash == 0) {
            return "/";
        }

        return path.substring(
            0,
            lastSlash
        );
    }

    private ProcessOutput execute(
            List<String> command)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
            new ProcessBuilder(command);

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

            while ((line = reader.readLine()) != null) {
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

    private static String normalizeAuthor(
            String email) {

        if (email == null) {
            return "unknown";
        }

        String normalized =
            email.trim()
                .toLowerCase(
                    Locale.ROOT
                );

        return normalized.isBlank()
            ? "unknown"
            : normalized;
    }

    public record NativeGitRevision(
        String commitId,
        LocalDate commitDate,
        String authorEmail,
        int added,
        int deleted,
        int changeSetSize,
        int nd,
        double entropy,
        boolean recoveredFromMerge
    ) {
    }

    private record RawRevision(
        String commitId,
        LocalDate commitDate,
        String authorEmail,
        int added,
        int deleted
    ) {
    }

    private record FirstParentRevision(
        String commitId,
        LocalDate commitDate,
        String authorEmail,
        int added,
        int deleted,
        boolean isMerge,
        String firstParentId,
        List<String> parentIds,
        String historicalPath
    ) {
    }

    private record CommitStats(
        int changeSetSize,
        int nd,
        double entropy
    ) {
    }

    private record NumStatEntry(
        int added,
        int deleted,
        String path,
        boolean numeric
    ) {
    }

    private record ProcessOutput(
        List<String> lines
    ) {
    }
}