package it.uniroma2.isw2.storm.szz;

import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SzzCsvWriter {

    private SzzCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<SzzBugIntroducingChange> changes)
            throws IOException {

        Path parent =
            outputPath.getParent();

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
                "IssueKey,"
                    + "FixCommitId,"
                    + "FixSelectionStrategy,"
                    + "ParentCommitId,"
                    + "FixedFilePath,"
                    + "BlamedFilePath,"
                    + "BugIntroducingCommitId,"
                    + "BlamedLineCount"
            );

            writer.newLine();

            for (SzzBugIntroducingChange change
                    : changes) {

                writer.write(csv(change.issueKey()));
                writer.write(",");
                writer.write(csv(change.fixCommitId()));
                writer.write(",");
                writer.write(
                    change.fixSelectionStrategy()
                        .name()
                );
                writer.write(",");
                writer.write(
                    csv(change.parentCommitId())
                );
                writer.write(",");
                writer.write(
                    csv(change.fixedFilePath())
                );
                writer.write(",");
                writer.write(
                    csv(change.blamedFilePath())
                );
                writer.write(",");
                writer.write(
                    csv(
                        change
                            .bugIntroducingCommitId()
                    )
                );
                writer.write(",");
                writer.write(
                    Integer.toString(
                        change.blamedLineCount()
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

        boolean quoted =
            value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r");

        if (!quoted) {
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