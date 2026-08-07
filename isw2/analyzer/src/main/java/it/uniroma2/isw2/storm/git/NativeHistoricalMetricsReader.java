package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.git.NativeGitHistoryReader.NativeGitRevision;
import it.uniroma2.isw2.storm.model.HistoricalMetrics;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NativeHistoricalMetricsReader {

    private final NativeGitHistoryReader historyReader;

    public NativeHistoricalMetricsReader(
            Path repositoryPath) {

        this.historyReader =
            new NativeGitHistoryReader(
                repositoryPath
            );
    }

    public HistoricalMetrics compute(
            String releaseCommit,
            LocalDate releaseDate,
            String filePath)
            throws IOException, InterruptedException {

        List<NativeGitRevision> revisions =
            historyReader.readHistory(
                releaseCommit,
                filePath
            );

        if (revisions.isEmpty()) {
            throw new IOException(
                "No Git revisions found for "
                    + filePath
                    + " at "
                    + releaseCommit
            );
        }

        long locTouched = 0;

        long locAdded = 0;
        long locDeleted = 0;

        long churn = 0;

        long changeSetSize = 0;

        long totalNd = 0;
        double totalEntropy = 0.0;

        int maxLocAdded = 0;
        int maxLocDeleted = 0;

        int maxChurn =
            Integer.MIN_VALUE;

        int maxChangeSet = 0;
        int maxNd = 0;

        double maxEntropy = 0.0;

        Set<String> authors =
            new HashSet<>();

        LocalDate oldestRevisionDate = null;

        double weightedAgeNumerator = 0.0;
        long weightedAgeDenominator = 0;

        for (NativeGitRevision revision
                : revisions) {

            int touched =
                revision.added()
                    + revision.deleted();

            int revisionChurn =
                revision.added()
                    - revision.deleted();

            locTouched +=
                touched;

            locAdded +=
                revision.added();

            locDeleted +=
                revision.deleted();

            churn +=
                revisionChurn;

            changeSetSize +=
                revision.changeSetSize();

            totalNd +=
                revision.nd();

            totalEntropy +=
                revision.entropy();

            maxLocAdded =
                Math.max(
                    maxLocAdded,
                    revision.added()
                );

            maxLocDeleted =
                Math.max(
                    maxLocDeleted,
                    revision.deleted()
                );

            maxChurn =
                Math.max(
                    maxChurn,
                    revisionChurn
                );

            maxChangeSet =
                Math.max(
                    maxChangeSet,
                    revision.changeSetSize()
                );

            maxNd =
                Math.max(
                    maxNd,
                    revision.nd()
                );

            maxEntropy =
                Math.max(
                    maxEntropy,
                    revision.entropy()
                );

            authors.add(
                revision.authorEmail()
            );

            if (
                oldestRevisionDate == null
                    || revision.commitDate()
                        .isBefore(
                            oldestRevisionDate
                        )
            ) {
                oldestRevisionDate =
                    revision.commitDate();
            }

            double revisionAgeWeeks =
                weeksBetween(
                    revision.commitDate(),
                    releaseDate
                );

            weightedAgeNumerator +=
                revisionAgeWeeks
                    * touched;

            weightedAgeDenominator +=
                touched;
        }

        int revisionCount =
            revisions.size();

        double ageWeeks =
            weeksBetween(
                oldestRevisionDate,
                releaseDate
            );

        double weightedAgeWeeks =
            weightedAgeDenominator == 0
                ? ageWeeks
                : weightedAgeNumerator
                    / weightedAgeDenominator;

        return new HistoricalMetrics(
            locTouched,
            revisionCount,
            authors.size(),

            locAdded,
            maxLocAdded,
            (double) locAdded
                / revisionCount,

            locDeleted,
            maxLocDeleted,
            (double) locDeleted
                / revisionCount,

            churn,
            maxChurn == Integer.MIN_VALUE
                ? 0
                : maxChurn,
            (double) churn
                / revisionCount,

            changeSetSize,
            maxChangeSet,
            (double) changeSetSize
                / revisionCount,

            (double) totalNd
                / revisionCount,
            maxNd,

            totalEntropy
                / revisionCount,
            maxEntropy,

            ageWeeks,
            weightedAgeWeeks
        );
    }

    public int cachedChangeSetCount() {
        return historyReader
            .cachedCommitStatsCount();
    }

    public int recoveredMergeIntroductionCount() {
        return historyReader
            .recoveredMergeIntroductionCount();
    }

    public int skippedDuplicateMergeIntroductionCount() {
        return historyReader
            .skippedDuplicateMergeIntroductionCount();
    }

    private static double weeksBetween(
            LocalDate start,
            LocalDate end) {

        long days =
            ChronoUnit.DAYS.between(
                start,
                end
            );

        return Math.max(
            0L,
            days
        ) / 7.0;
    }
}