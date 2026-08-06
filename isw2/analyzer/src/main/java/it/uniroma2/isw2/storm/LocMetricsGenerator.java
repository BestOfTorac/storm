package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.git.GitJavaSourceReader;
import it.uniroma2.isw2.storm.inventory.JavaSourceClassifier;
import it.uniroma2.isw2.storm.metrics.JavaLocCounter;
import it.uniroma2.isw2.storm.metrics.LocMetricsCsvWriter;
import it.uniroma2.isw2.storm.model.GitJavaSourceFile;
import it.uniroma2.isw2.storm.model.LocMetricEntry;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;

public final class LocMetricsGenerator {

    private LocMetricsGenerator() {
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
                "Usage: LocMetricsGenerator "
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
                "isw2/datasets/loc_metrics.csv"
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

            List<LocMetricEntry> metrics =
                new ArrayList<>();

            try (
                GitJavaSourceReader sourceReader =
                    GitJavaSourceReader.open(
                        repositoryPath
                    )
            ) {
                int currentRelease = 0;

                for (ReleaseCatalogEntry release
                        : releases) {

                    currentRelease++;

                    List<GitJavaSourceFile> sources =
                        sourceReader
                            .readJavaFilesAtCommit(
                                release.gitCommitId(),
                                filePath ->
                                    JavaSourceClassifier
                                        .classify(filePath)
                                        == SourceCategory
                                            .PRODUCTION
                            );

                    IntSummaryStatistics statistics =
                        new IntSummaryStatistics();

                    int zeroLocFiles = 0;

                    for (GitJavaSourceFile source
                            : sources) {

                        int loc =
                            JavaLocCounter.count(
                                source.content()
                            );

                        statistics.accept(loc);

                        if (loc == 0) {
                            zeroLocFiles++;
                        }

                        metrics.add(
                            new LocMetricEntry(
                                release.index(),
                                release.version(),
                                release.gitCommitId(),
                                source.filePath(),
                                loc
                            )
                        );
                    }

                    printReleaseSummary(
                        currentRelease,
                        releases.size(),
                        release,
                        statistics,
                        zeroLocFiles
                    );
                }
            }

            LocMetricsCsvWriter.write(
                outputPath,
                metrics
            );

            System.out.printf(
                "LOC observations: %d%n",
                metrics.size()
            );

            System.out.printf(
                "LOC metrics written to: %s%n",
                outputPath
            );

            return 0;

        } catch (IOException exception) {
            System.err.printf(
                "LOC generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }

    private static void printReleaseSummary(
            int currentRelease,
            int totalReleases,
            ReleaseCatalogEntry release,
            IntSummaryStatistics statistics,
            int zeroLocFiles) {

        System.out.printf(
            Locale.ROOT,
            "[%02d/%02d] %-18s classes=%d, "
                + "loc-total=%d, loc-average=%.2f, "
                + "loc-min=%d, loc-max=%d, "
                + "zero-loc=%d%n",
            currentRelease,
            totalReleases,
            release.version(),
            statistics.getCount(),
            statistics.getSum(),
            statistics.getAverage(),
            statistics.getMin(),
            statistics.getMax(),
            zeroLocFiles
        );
    }
}