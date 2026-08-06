package it.uniroma2.isw2.storm.release;

import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class ReleaseCatalogCsvWriter {

    private ReleaseCatalogCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<ReleaseCatalogEntry> entries)
            throws IOException {

        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    outputPath,
                    StandardCharsets.UTF_8
                )
        ) {
            writer.write(
                "Index,Version,ReleaseDate,"
                    + "ReleaseDateSource,"
                    + "JiraVersionId,"
                    + "GitHubTag,"
                    + "GitTag,"
                    + "GitCommitId,"
                    + "GitCommitDate,"
                    + "DatasetIncluded"
            );

            writer.newLine();

            for (ReleaseCatalogEntry entry
                    : entries) {

                writer.write(
                    Integer.toString(
                        entry.index()
                    )
                );

                writer.write(",");
                writer.write(csv(entry.version()));

                writer.write(",");
                writer.write(
                    csv(
                        entry.releaseDate()
                            .toString()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        entry.releaseDateSource()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        entry.jiraVersionId()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(entry.githubTag())
                );

                writer.write(",");
                writer.write(
                    csv(entry.gitTag())
                );

                writer.write(",");
                writer.write(
                    csv(entry.gitCommitId())
                );

                writer.write(",");
                writer.write(
                    csv(
                        formatDate(
                            entry.gitCommitDate()
                        )
                    )
                );

                writer.write(",");
                writer.write(
                    Boolean.toString(
                        entry.includedInDataset()
                    )
                );

                writer.newLine();
            }
        }
    }

    private static String formatDate(
            LocalDate date) {

        return date == null
            ? ""
            : date.toString();
    }

    private static String csv(String value) {
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
            + value.replace("\"", "\"\"")
            + "\"";
    }
}