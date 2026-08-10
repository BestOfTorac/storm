package it.uniroma2.isw2.storm.buggy;

import it.uniroma2.isw2.storm.model.BuggyLabelEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BuggyLabelsCsvWriter {

    private BuggyLabelsCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<BuggyLabelEntry> rows)
            throws IOException {

        Files.createDirectories(
            outputPath.getParent()
        );

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    outputPath,
                    StandardCharsets.UTF_8
                )
        ) {
            writer.write(
                "ReleaseIndex,Version,CommitId,"
                    + "FilePath,BUGGY"
            );

            writer.newLine();

            for (BuggyLabelEntry row
                    : rows) {

                writer.write(
                    Integer.toString(
                        row.releaseIndex()
                    )
                );

                writer.write(',');
                writer.write(
                    escape(row.version())
                );

                writer.write(',');
                writer.write(
                    escape(row.commitId())
                );

                writer.write(',');
                writer.write(
                    escape(row.filePath())
                );

                writer.write(',');
                writer.write(
                    row.buggy()
                        ? "YES"
                        : "NO"
                );

                writer.newLine();
            }
        }
    }

    private static String escape(
            String value) {

        if (value == null) {
            return "";
        }

        if (
            value.indexOf(',') < 0
                && value.indexOf('"') < 0
                && value.indexOf('\n') < 0
                && value.indexOf('\r') < 0
        ) {
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