package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.git.GitHistoryMetricsReader;
import it.uniroma2.isw2.storm.git.RepositoryInspector;
import it.uniroma2.isw2.storm.inventory.JavaSourceClassifier;
import it.uniroma2.isw2.storm.metrics.HistoricalMetricsCsvWriter;
import it.uniroma2.isw2.storm.model.HistoricalMetricEntry;
import it.uniroma2.isw2.storm.model.HistoricalMetrics;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HistoricalMetricsGenerator {

    private HistoricalMetricsGenerator() {
        // Application entry point.
    }

    public static void main(String[] args) {
        int exitCode = run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        if (args.length != 1) {
            System.err.println(
                "Usage: HistoricalMetricsGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path catalogPath =
            repositoryPath.resolve(
                "isw2/datasets/release_catalog.csv"
            );

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/historical_metrics.csv"
            );

        try {
            List<ReleaseCatalogEntry> releases =
                ReleaseCatalogCsvReader
                    .read(catalogPath)
                    .stream()
                    .filter(
                        ReleaseCatalogEntry
                            ::includedInDataset
                    )
                    .sorted(
                        Comparator.comparingInt(
                            ReleaseCatalogEntry::index
                        )
                    )
                    .toList();

            List<HistoricalMetricEntry> output =
                new ArrayList<>();

            try (
                RepositoryInspector inspector =
                    RepositoryInspector.open(
                        repositoryPath
                    );

                GitHistoryMetricsReader historyReader =
                    GitHistoryMetricsReader.open(
                        repositoryPath
                    )
            ) {
                int currentRelease = 0;

                for (ReleaseCatalogEntry release
                        : releases) {

                    currentRelease++;

                    Set<String> productionPaths =
                        inspector
                            .readJavaFilePathsAtCommit(
                                release.gitCommitId()
                            )
                            .stream()
                            .filter(
                                path ->
                                    JavaSourceClassifier
                                        .classify(path)
                                        == SourceCategory
                                            .PRODUCTION
                            )
                            .collect(
                                java.util.stream.Collectors
                                    .toCollection(
                                        LinkedHashSet::new
                                    )
                            );

                    GitHistoryMetricsReader
                        .HistoryAnalysisResult result =
                            historyReader.compute(
                                release.gitCommitId(),
                                release.releaseDate(),
                                productionPaths
                            );

                    for (
                        Map.Entry<
                            String,
                            HistoricalMetrics
                        > metric :
                            result.metrics()
                                .entrySet()
                    ) {
                        output.add(
                            new HistoricalMetricEntry(
                                release.index(),
                                release.version(),
                                release.gitCommitId(),
                                metric.getKey(),
                                metric.getValue()
                            )
                        );
                    }

                    long zeroRevisionClasses =
                        result.metrics()
                            .values()
                            .stream()
                            .filter(
                                metric ->
                                    metric.revisions()
                                        == 0
                            )
                            .count();

                    System.out.printf(
                            "[%02d/%02d] %-18s "
                                    + "classes=%d, commits=%d, "
                                    + "merges-skipped=%d, "
                                    + "recovered-introductions=%d, "
                                    + "zero-revision=%d%n",
                            currentRelease,
                            releases.size(),
                            release.version(),
                            productionPaths.size(),
                            result.commitsVisited(),
                            result.mergeCommitsSkipped(),
                            result.recoveredIntroductions(),
                            zeroRevisionClasses
                    );
                }
            }

            output.sort(
                Comparator
                    .comparingInt(
                        HistoricalMetricEntry
                            ::releaseIndex
                    )
                    .thenComparing(
                        HistoricalMetricEntry
                            ::filePath
                    )
            );

            HistoricalMetricsCsvWriter.write(
                outputPath,
                output
            );

            System.out.printf(
                "Historical observations: %d%n",
                output.size()
            );

            System.out.printf(
                "Historical metrics written to: %s%n",
                outputPath
            );

            return 0;

        } catch (IOException exception) {
            System.err.printf(
                "Historical metric generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}