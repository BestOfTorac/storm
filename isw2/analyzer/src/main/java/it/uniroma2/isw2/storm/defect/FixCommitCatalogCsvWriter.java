package it.uniroma2.isw2.storm.defect;

import it.uniroma2.isw2.storm.model.SelectedFixCommit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FixCommitCatalogCsvWriter {

    private FixCommitCatalogCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<SelectedFixCommit> fixes)
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
                "IssueKey,"
                    + "CommitId,"
                    + "CommitDate,"
                    + "SelectionStrategy,"
                    + "ParentCount,"
                    + "IsMerge,"
                    + "IssueInSubject,"
                    + "ProductionJavaFiles,"
                    + "ProductionJavaPaths,"
                    + "Subject"
            );

            writer.newLine();

            for (
                SelectedFixCommit fix : fixes
            ) {
                writer.write(
                    csv(fix.issueKey())
                );
                writer.write(",");

                writer.write(
                    csv(fix.commitId())
                );
                writer.write(",");

                writer.write(
                    csv(
                        fix.commitDate()
                            .toString()
                    )
                );
                writer.write(",");

                writer.write(
                    fix.selectionStrategy()
                        .name()
                );
                writer.write(",");

                writer.write(
                    Integer.toString(
                        fix.parentCount()
                    )
                );
                writer.write(",");

                writer.write(
                    Boolean.toString(
                        fix.merge()
                    )
                );
                writer.write(",");

                writer.write(
                    Boolean.toString(
                        fix.issueInSubject()
                    )
                );
                writer.write(",");

                writer.write(
                    Integer.toString(
                        fix.productionJavaFiles()
                            .size()
                    )
                );
                writer.write(",");

                writer.write(
                    csv(
                        String.join(
                            " | ",
                            fix.productionJavaFiles()
                        )
                    )
                );
                writer.write(",");

                writer.write(
                    csv(fix.subject())
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