package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.defect.FixCommitCatalogCsvReader;
import it.uniroma2.isw2.storm.git.NativeNfixHistoryReader;
import it.uniroma2.isw2.storm.git.RepositoryInspector;
import it.uniroma2.isw2.storm.inventory.JavaSourceClassifier;
import it.uniroma2.isw2.storm.metrics.NfixMetricsCsvWriter;
import it.uniroma2.isw2.storm.model.NfixMetricEntry;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.model.SelectedFixCommit;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NfixMetricsGenerator {

    private NfixMetricsGenerator() {
        // Application entry point.
    }

    public static void main(String[] args) {

        int exitCode =
            run(args);

        if (exitCode != 0) {
            System.exit(
                exitCode
            );
        }
    }

    private static int run(
            String[] args) {

        if (args.length != 1) {

            System.err.println(
                "Usage: NfixMetricsGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path releaseCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/release_catalog.csv"
            );

        Path fixCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/fix_commit_catalog.csv"
            );

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/nfix_metrics.csv"
            );

        try {
            List<ReleaseCatalogEntry> releases =
                ReleaseCatalogCsvReader
                    .read(
                        releaseCatalogPath
                    )
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

            List<SelectedFixCommit> fixes =
                FixCommitCatalogCsvReader.read(
                    fixCatalogPath
                );

            NativeNfixHistoryReader nfixReader =
                new NativeNfixHistoryReader(
                    repositoryPath,
                    fixes
                );

            List<NfixMetricEntry> output =
                new ArrayList<>();

            int positiveObservations = 0;
            int maximumNfix = 0;

            try (
                RepositoryInspector inspector =
                    RepositoryInspector.open(
                        repositoryPath
                    )
            ) {
                int releasePosition = 0;

                for (
                    ReleaseCatalogEntry release
                        : releases
                ) {
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

                    for (
                        String filePath
                            : productionPaths
                    ) {
                        classPosition++;

                        NativeNfixHistoryReader
                            .NfixResult result =
                                nfixReader.compute(
                                    release.gitCommitId(),
                                    filePath
                                );

                        int nfix =
                            result.nfix();

                        if (nfix > 0) {
                            positiveObservations++;
                        }

                        maximumNfix =
                            Math.max(
                                maximumNfix,
                                nfix
                            );

                        output.add(
                            new NfixMetricEntry(
                                release.index(),
                                release.version(),
                                release.gitCommitId(),
                                filePath,
                                nfix
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
                                    + "normal-history=%d, "
                                    + "first-parent-history=%d%n",
                                releasePosition,
                                releases.size(),
                                release.version(),
                                classPosition,
                                productionPaths.size(),
                                nfixReader
                                    .nonMergeHistoryReadCount(),
                                nfixReader
                                    .firstParentHistoryReadCount()
                            );
                        }
                    }
                }
            }

            output.sort(
                Comparator
                    .comparingInt(
                        NfixMetricEntry::releaseIndex
                    )
                    .thenComparing(
                        NfixMetricEntry::filePath
                    )
            );

            NfixMetricsCsvWriter.write(
                outputPath,
                output
            );

            System.out.printf(
                "NFIX observations: %d%n",
                output.size()
            );

            System.out.printf(
                "Observations with NFIX > 0: %d%n",
                positiveObservations
            );

            System.out.printf(
                "Maximum NFIX: %d%n",
                maximumNfix
            );

            System.out.printf(
                "Non-merge history reads: %d%n",
                nfixReader
                    .nonMergeHistoryReadCount()
            );

            System.out.printf(
                "First-parent history reads: %d%n",
                nfixReader
                    .firstParentHistoryReadCount()
            );

            System.out.printf(
                "Releases with reachable "
                    + "first-parent fix: %d%n",
                nfixReader
                    .releasesWithReachableFirstParentFixCount()
            );

            System.out.printf(
                "NFIX metrics written to: %s%n",
                outputPath
            );

            return 0;

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "NFIX generation interrupted."
            );

            return 1;

        } catch (
                IOException
                | IllegalArgumentException exception) {

            System.err.printf(
                "NFIX generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}