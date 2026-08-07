package it.uniroma2.isw2.storm.metrics;

import it.uniroma2.isw2.storm.model.HistoricalMetricEntry;
import it.uniroma2.isw2.storm.model.HistoricalMetrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class HistoricalMetricsCsvWriter {

    private HistoricalMetricsCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<HistoricalMetricEntry> entries)
            throws IOException {

        Path parent =
            outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(
                parent
            );
        }

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    outputPath,
                    StandardCharsets.UTF_8
                )
        ) {
            writer.write(
                "ReleaseIndex,"
                    + "Version,"
                    + "CommitId,"
                    + "FilePath,"
                    + "LOC_TOUCHED,"
                    + "NR,"
                    + "NAUTH,"
                    + "LOC_ADDED,"
                    + "MAX_LOC_ADDED,"
                    + "AVG_LOC_ADDED,"
                    + "LOC_DELETED,"
                    + "MAX_LOC_DELETED,"
                    + "AVG_LOC_DELETED,"
                    + "CHURN,"
                    + "MAX_CHURN,"
                    + "AVG_CHURN,"
                    + "CHANGE_SET_SIZE,"
                    + "MAX_CHANGE_SET,"
                    + "AVG_CHANGE_SET,"
                    + "AVG_ND,"
                    + "MAX_ND,"
                    + "AVG_ENTROPY,"
                    + "MAX_ENTROPY,"
                    + "AGE_WEEKS,"
                    + "WEIGHTED_AGE_WEEKS"
            );

            writer.newLine();

            for (HistoricalMetricEntry entry
                    : entries) {

                HistoricalMetrics metrics =
                    entry.metrics();

                writer.write(
                    Integer.toString(
                        entry.releaseIndex()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(entry.version())
                );

                writer.write(",");
                writer.write(
                    csv(entry.commitId())
                );

                writer.write(",");
                writer.write(
                    csv(entry.filePath())
                );

                writer.write(",");
                writer.write(
                    Long.toString(
                        metrics.locTouched()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        metrics.revisions()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        metrics.authors()
                    )
                );

                writer.write(",");
                writer.write(
                    Long.toString(
                        metrics.locAdded()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        metrics.maxLocAdded()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.avgLocAdded()
                    )
                );

                writer.write(",");
                writer.write(
                    Long.toString(
                        metrics.locDeleted()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        metrics.maxLocDeleted()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.avgLocDeleted()
                    )
                );

                writer.write(",");
                writer.write(
                    Long.toString(
                        metrics.churn()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        metrics.maxChurn()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.avgChurn()
                    )
                );

                writer.write(",");
                writer.write(
                    Long.toString(
                        metrics.changeSetSize()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        metrics.maxChangeSet()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.avgChangeSet()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.avgNd()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        metrics.maxNd()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.avgEntropy()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.maxEntropy()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.ageWeeks()
                    )
                );

                writer.write(",");
                writer.write(
                    Double.toString(
                        metrics.weightedAgeWeeks()
                    )
                );

                writer.newLine();
            }
        }
    }

    private static String csv(
            String value) {

        if (value == null) {
            return "";
        }

        boolean requiresQuotes =
            value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r");

        if (!requiresQuotes) {
            return value;
        }

        return "\""
            + value.replace(
                "\"",
                "\"\""
            )
            + "\"";
    }
}