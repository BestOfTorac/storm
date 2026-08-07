package it.uniroma2.isw2.storm.metrics;

import it.uniroma2.isw2.storm.model.NfixMetricEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class NfixMetricsCsvWriter {

    private NfixMetricsCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<NfixMetricEntry> entries)
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
                    + "NFIX"
            );

            writer.newLine();

            for (NfixMetricEntry entry
                    : entries) {

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
                    Integer.toString(
                        entry.nfix()
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