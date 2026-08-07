package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.model.HistoricalMetrics;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GitHistoryMetricsReader
        implements AutoCloseable {

    private final Repository repository;

    private GitHistoryMetricsReader(
            Repository repository) {

        this.repository = repository;
    }

    public static GitHistoryMetricsReader open(
            Path repositoryRoot)
            throws IOException {

        Path gitDirectory =
            repositoryRoot.resolve(".git");

        if (!Files.exists(gitDirectory)) {
            throw new IOException(
                "Git directory not found: "
                    + gitDirectory
            );
        }

        Repository repository =
            new FileRepositoryBuilder()
                .setGitDir(gitDirectory.toFile())
                .setMustExist(true)
                .build();

        return new GitHistoryMetricsReader(
            repository
        );
    }

    public HistoryAnalysisResult compute(
            String releaseCommitId,
            LocalDate releaseDate,
            Set<String> currentFilePaths)
            throws IOException {

        Objects.requireNonNull(
            releaseCommitId,
            "Release commit cannot be null."
        );

        Objects.requireNonNull(
            releaseDate,
            "Release date cannot be null."
        );

        Objects.requireNonNull(
            currentFilePaths,
            "Current file paths cannot be null."
        );

        ObjectId releaseObjectId =
            repository.resolve(
                releaseCommitId.trim()
            );

        if (releaseObjectId == null) {
            throw new IOException(
                "Unable to resolve release commit: "
                    + releaseCommitId
            );
        }

        Map<String, List<RevisionEvent>>
            eventsByPath = new HashMap<>();

        for (String path : currentFilePaths) {
            eventsByPath.put(
                path,
                new ArrayList<>()
            );
        }

        Set<String> activePaths =
            new HashSet<>(currentFilePaths);

        int commitsVisited = 0;
        int mergeCommitsSkipped = 0;

        try (
            RevWalk revWalk =
                new RevWalk(repository);

            ObjectReader objectReader =
                repository.newObjectReader();

            DiffFormatter diffFormatter =
                new DiffFormatter(
                    DisabledOutputStream.INSTANCE
                )
        ) {
            diffFormatter.setRepository(repository);
            diffFormatter.setDetectRenames(true);
            diffFormatter.setDiffComparator(
                RawTextComparator.DEFAULT
            );

            RevCommit releaseCommit =
                revWalk.parseCommit(
                    releaseObjectId
                );

            revWalk.markStart(releaseCommit);

            for (RevCommit commit : revWalk) {
                if (activePaths.isEmpty()) {
                    break;
                }

                commitsVisited++;

                if (commit.getParentCount() > 1) {
                    mergeCommitsSkipped++;
                    continue;
                }

                AbstractTreeIterator oldTree;

                if (commit.getParentCount() == 0) {
                    oldTree =
                        new EmptyTreeIterator();

                } else {
                    RevCommit parent =
                        revWalk.parseCommit(
                            commit.getParent(0)
                                .getId()
                        );

                    oldTree =
                        createTreeParser(
                            objectReader,
                            parent
                        );
                }

                CanonicalTreeParser newTree =
                    createTreeParser(
                        objectReader,
                        commit
                    );

                List<DiffEntry> diffs =
                    diffFormatter.scan(
                        oldTree,
                        newTree
                    );

                int changeSetSize =
                    diffs.size();

                LocalDate commitDate =
                    commitDate(commit);

                String author =
                    authorKey(
                        commit.getAuthorIdent()
                    );

                for (DiffEntry diff : diffs) {
                    String newPath =
                        diff.getNewPath();

                    if (
                        DiffEntry.DEV_NULL.equals(
                            newPath
                        )
                        || !activePaths.contains(
                            newPath
                        )
                    ) {
                        continue;
                    }

                    LineChanges lineChanges =
                        countLineChanges(
                            diffFormatter,
                            diff
                        );

                    eventsByPath
                        .get(newPath)
                        .add(
                            new RevisionEvent(
                                author,
                                commitDate,
                                lineChanges.added(),
                                lineChanges.deleted(),
                                changeSetSize
                            )
                        );

                    if (
                        isPathCreationBoundary(
                            diff.getChangeType()
                        )
                    ) {
                        activePaths.remove(
                            newPath
                        );
                    }
                }
            }
        }

        Set<String> unresolvedPaths =
            new HashSet<>();

        for (
            Map.Entry<String, List<RevisionEvent>>
                entry : eventsByPath.entrySet()
        ) {
            if (entry.getValue().isEmpty()) {
                unresolvedPaths.add(
                    entry.getKey()
                );
            }
        }

        int recoveredIntroductions =
            recoverFirstParentIntroductions(
                releaseObjectId,
                unresolvedPaths,
                eventsByPath
            );

        Map<String, HistoricalMetrics> metrics =
            new LinkedHashMap<>();

        currentFilePaths.stream()
            .sorted()
            .forEach(
                path -> metrics.put(
                    path,
                    aggregate(
                        eventsByPath.get(path),
                        releaseDate
                    )
                )
            );

        return new HistoryAnalysisResult(
            Map.copyOf(metrics),
            commitsVisited,
            mergeCommitsSkipped,
            recoveredIntroductions
        );
    }

    private int recoverFirstParentIntroductions(
            ObjectId releaseObjectId,
            Set<String> unresolvedPaths,
            Map<String, List<RevisionEvent>>
                eventsByPath)
            throws IOException {

        if (unresolvedPaths.isEmpty()) {
            return 0;
        }

        Set<String> remaining =
            new HashSet<>(unresolvedPaths);

        int recovered = 0;

        try (
            RevWalk revWalk =
                new RevWalk(repository);

            ObjectReader objectReader =
                repository.newObjectReader();

            DiffFormatter diffFormatter =
                new DiffFormatter(
                    DisabledOutputStream.INSTANCE
                )
        ) {
            diffFormatter.setRepository(repository);
            diffFormatter.setDetectRenames(true);
            diffFormatter.setDiffComparator(
                RawTextComparator.DEFAULT
            );

            RevCommit current =
                revWalk.parseCommit(
                    releaseObjectId
                );

            while (
                current != null
                    && !remaining.isEmpty()
            ) {
                AbstractTreeIterator oldTree;
                RevCommit firstParent = null;

                if (current.getParentCount() == 0) {
                    oldTree =
                        new EmptyTreeIterator();

                } else {
                    firstParent =
                        revWalk.parseCommit(
                            current.getParent(0)
                                .getId()
                        );

                    oldTree =
                        createTreeParser(
                            objectReader,
                            firstParent
                        );
                }

                CanonicalTreeParser newTree =
                    createTreeParser(
                        objectReader,
                        current
                    );

                List<DiffEntry> diffs =
                    diffFormatter.scan(
                        oldTree,
                        newTree
                    );

                int changeSetSize =
                    diffs.size();

                LocalDate currentDate =
                    commitDate(current);

                String author =
                    authorKey(
                        current.getAuthorIdent()
                    );

                for (DiffEntry diff : diffs) {
                    String newPath =
                        diff.getNewPath();

                    if (
                        DiffEntry.DEV_NULL.equals(
                            newPath
                        )
                        || !remaining.contains(
                            newPath
                        )
                    ) {
                        continue;
                    }

                    if (
                        !isPathCreationBoundary(
                            diff.getChangeType()
                        )
                    ) {
                        continue;
                    }

                    LineChanges lineChanges =
                        countLineChanges(
                            diffFormatter,
                            diff
                        );

                    eventsByPath
                        .get(newPath)
                        .add(
                            new RevisionEvent(
                                author,
                                currentDate,
                                lineChanges.added(),
                                lineChanges.deleted(),
                                changeSetSize
                            )
                        );

                    remaining.remove(newPath);
                    recovered++;
                }

                current = firstParent;
            }
        }

        return recovered;
    }

    private static boolean isPathCreationBoundary(
            DiffEntry.ChangeType changeType) {

        return changeType
                == DiffEntry.ChangeType.ADD
            || changeType
                == DiffEntry.ChangeType.RENAME
            || changeType
                == DiffEntry.ChangeType.COPY;
    }

    private static HistoricalMetrics aggregate(
            List<RevisionEvent> events,
            LocalDate releaseDate) {

        if (events == null || events.isEmpty()) {
            return new HistoricalMetrics(
                0,
                0,
                0,
                0,
                0,
                0.0,
                0,
                0,
                0.0,
                0,
                0,
                0.0,
                0.0,
                0.0
            );
        }

        long locTouched = 0;
        long locAdded = 0;
        long churn = 0;
        long changeSetSize = 0;

        int maxLocAdded = 0;
        int maxChurn = Integer.MIN_VALUE;
        int maxChangeSet = 0;

        Set<String> authors =
            new HashSet<>();

        LocalDate firstRevisionDate = null;

        double weightedAgeNumerator = 0.0;
        long weightedAgeDenominator = 0;

        for (RevisionEvent event : events) {
            int touched =
                event.added()
                    + event.deleted();

            int revisionChurn =
                event.added()
                    - event.deleted();

            locTouched += touched;
            locAdded += event.added();
            churn += revisionChurn;

            changeSetSize +=
                event.changeSetSize();

            maxLocAdded =
                Math.max(
                    maxLocAdded,
                    event.added()
                );

            maxChurn =
                Math.max(
                    maxChurn,
                    revisionChurn
                );

            maxChangeSet =
                Math.max(
                    maxChangeSet,
                    event.changeSetSize()
                );

            authors.add(
                event.authorKey()
            );

            if (
                firstRevisionDate == null
                    || event.commitDate()
                        .isBefore(
                            firstRevisionDate
                        )
            ) {
                firstRevisionDate =
                    event.commitDate();
            }

            double revisionAgeWeeks =
                weeksBetween(
                    event.commitDate(),
                    releaseDate
                );

            weightedAgeNumerator +=
                revisionAgeWeeks * touched;

            weightedAgeDenominator +=
                touched;
        }

        int revisions =
            events.size();

        double ageWeeks =
            weeksBetween(
                firstRevisionDate,
                releaseDate
            );

        double weightedAgeWeeks =
            weightedAgeDenominator == 0
                ? ageWeeks
                : weightedAgeNumerator
                    / weightedAgeDenominator;

        return new HistoricalMetrics(
            locTouched,
            revisions,
            authors.size(),
            locAdded,
            maxLocAdded,
            (double) locAdded / revisions,
            churn,
            maxChurn == Integer.MIN_VALUE
                ? 0
                : maxChurn,
            (double) churn / revisions,
            changeSetSize,
            maxChangeSet,
            (double) changeSetSize
                / revisions,
            ageWeeks,
            weightedAgeWeeks
        );
    }

    private static LocalDate commitDate(
            RevCommit commit) {

        return Instant.ofEpochSecond(
            commit.getCommitTime()
        )
        .atZone(ZoneOffset.UTC)
        .toLocalDate();
    }

    private static double weeksBetween(
            LocalDate start,
            LocalDate end) {

        long days =
            ChronoUnit.DAYS.between(
                start,
                end
            );

        return Math.max(0L, days)
            / 7.0;
    }

    private static LineChanges countLineChanges(
            DiffFormatter formatter,
            DiffEntry diff)
            throws IOException {

        int added = 0;
        int deleted = 0;

        for (
            Edit edit :
                formatter
                    .toFileHeader(diff)
                    .toEditList()
        ) {
            deleted +=
                edit.getEndA()
                    - edit.getBeginA();

            added +=
                edit.getEndB()
                    - edit.getBeginB();
        }

        return new LineChanges(
            added,
            deleted
        );
    }

    private static CanonicalTreeParser
            createTreeParser(
                ObjectReader reader,
                RevCommit commit)
            throws IOException {

        CanonicalTreeParser parser =
            new CanonicalTreeParser();

        parser.reset(
            reader,
            commit.getTree().getId()
        );

        return parser;
    }

    private static String authorKey(
            PersonIdent author) {

        if (author == null) {
            return "unknown";
        }

        String email =
            author.getEmailAddress();

        if (
            email != null
                && !email.isBlank()
        ) {
            return email
                .trim()
                .toLowerCase(Locale.ROOT);
        }

        String name =
            author.getName();

        if (
            name != null
                && !name.isBlank()
        ) {
            return name
                .trim()
                .toLowerCase(Locale.ROOT);
        }

        return "unknown";
    }

    @Override
    public void close() {
        repository.close();
    }

    public record HistoryAnalysisResult(
        Map<String, HistoricalMetrics> metrics,
        int commitsVisited,
        int mergeCommitsSkipped,
        int recoveredIntroductions
    ) {
    }

    private record RevisionEvent(
        String authorKey,
        LocalDate commitDate,
        int added,
        int deleted,
        int changeSetSize
    ) {
    }

    private record LineChanges(
        int added,
        int deleted
    ) {
    }
}