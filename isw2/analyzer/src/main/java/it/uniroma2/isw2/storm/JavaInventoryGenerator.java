package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.git.RepositoryInspector;
import it.uniroma2.isw2.storm.inventory.JavaInventoryCsvWriter;
import it.uniroma2.isw2.storm.inventory.JavaSourceClassifier;
import it.uniroma2.isw2.storm.model.JavaFileInventoryEntry;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class JavaInventoryGenerator {

    private JavaInventoryGenerator() {
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
                "Usage: JavaInventoryGenerator "
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
                "isw2/datasets/java_class_inventory.csv"
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

            if (releases.isEmpty()) {
                throw new IOException(
                    "The release catalog contains "
                        + "no dataset releases."
                );
            }

            List<JavaFileInventoryEntry> inventory =
                new ArrayList<>();

            try (
                RepositoryInspector inspector =
                    RepositoryInspector.open(
                        repositoryPath
                    )
            ) {
                int currentRelease = 0;

                for (ReleaseCatalogEntry release
                        : releases) {

                    currentRelease++;

                    List<String> javaFilePaths =
                        inspector
                            .readJavaFilePathsAtCommit(
                                release.gitCommitId()
                            );

                    Map<SourceCategory, Integer> counts =
                        emptyCategoryCounts();

                    for (String filePath
                            : javaFilePaths) {

                        SourceCategory category =
                            JavaSourceClassifier
                                .classify(filePath);

                        counts.merge(
                            category,
                            1,
                            Integer::sum
                        );

                        inventory.add(
                            new JavaFileInventoryEntry(
                                release.index(),
                                release.version(),
                                release.gitCommitId(),
                                filePath,
                                category
                            )
                        );
                    }

                    printReleaseSummary(
                        currentRelease,
                        releases.size(),
                        release,
                        javaFilePaths.size(),
                        counts
                    );
                }
            }

            JavaInventoryCsvWriter.write(
                outputPath,
                inventory
            );

            printOverallSummary(
                inventory,
                outputPath
            );

            return 0;

        } catch (IOException exception) {
            System.err.printf(
                "Java inventory generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }

    private static Map<SourceCategory, Integer>
            emptyCategoryCounts() {

        Map<SourceCategory, Integer> counts =
            new EnumMap<>(SourceCategory.class);

        for (SourceCategory category
                : SourceCategory.values()) {

            counts.put(category, 0);
        }

        return counts;
    }

    private static void printReleaseSummary(
            int currentRelease,
            int totalReleases,
            ReleaseCatalogEntry release,
            int totalFiles,
            Map<SourceCategory, Integer> counts) {

        System.out.printf(
            "[%02d/%02d] %-18s total=%d, "
                + "production=%d, test=%d, "
                + "example=%d, generated=%d, "
                + "other=%d%n",
            currentRelease,
            totalReleases,
            release.version(),
            totalFiles,
            counts.get(SourceCategory.PRODUCTION),
            counts.get(SourceCategory.TEST),
            counts.get(SourceCategory.EXAMPLE),
            counts.get(SourceCategory.GENERATED),
            counts.get(SourceCategory.OTHER)
        );
    }

    private static void printOverallSummary(
            List<JavaFileInventoryEntry> inventory,
            Path outputPath) {

        Map<SourceCategory, Integer> totals =
            emptyCategoryCounts();

        for (JavaFileInventoryEntry entry
                : inventory) {

            totals.merge(
                entry.sourceCategory(),
                1,
                Integer::sum
            );
        }

        System.out.printf(
            "Inventory entries: %d%n",
            inventory.size()
        );

        for (SourceCategory category
                : SourceCategory.values()) {

            System.out.printf(
                "  - %-10s %d%n",
                category,
                totals.get(category)
            );
        }

        System.out.printf(
            "Inventory written to: %s%n",
            outputPath
        );
    }
}