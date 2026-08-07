package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.git.NativeHistoricalMetricsReader;
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
import java.util.List;

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

            NativeHistoricalMetricsReader
                metricsReader =
                    new NativeHistoricalMetricsReader(
                        repositoryPath
                    );

            try (
                RepositoryInspector inspector =
                    RepositoryInspector.open(
                        repositoryPath
                    )
            ) {
                int releasePosition = 0;

                for (ReleaseCatalogEntry release
                        : releases) {

                    releasePosition++;

                    List<String> productionPaths =
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
                            .sorted()
                            .toList();

                    int classPosition = 0;

                    for (String filePath
                            : productionPaths) {

                        classPosition++;

                        HistoricalMetrics metrics =
                            metricsReader.compute(
                                release.gitCommitId(),
                                release.releaseDate(),
                                filePath
                            );

                        output.add(
                            new HistoricalMetricEntry(
                                release.index(),
                                release.version(),
                                release.gitCommitId(),
                                filePath,
                                metrics
                            )
                        );

                        if (
                            classPosition % 100 == 0
                                || classPosition
                                    == productionPaths.size()
                        ) {
                            System.out.printf(
                                "[%02d/%02d] %-18s "
                                    + "classes=%d/%d, "
                                    + "cached-commits=%d, "
                                    + "merge-recovered=%d, "
                                    + "merge-duplicates-skipped=%d%n",
                                releasePosition,
                                releases.size(),
                                release.version(),
                                classPosition,
                                productionPaths.size(),
                                metricsReader
                                    .cachedChangeSetCount(),
                                metricsReader
                                    .recoveredMergeIntroductionCount(),
                                metricsReader
                                    .skippedDuplicateMergeIntroductionCount()
                            );
                        }
                    }
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

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "Historical metric generation interrupted."
            );

            return 1;

        } catch (IOException exception) {

            System.err.printf(
                "Historical metric generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}