package it.uniroma2.isw2.storm.defect;

import it.uniroma2.isw2.storm.model.FixCommitCandidate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FixCommitCandidateCsvWriter {

    private FixCommitCandidateCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<FixCommitCandidate> candidates)
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
                    + "ParentCount,"
                    + "IsMerge,"
                    + "IsRevert,"
                    + "IssueInSubject,"
                    + "ChangedFiles,"
                    + "ProductionJavaFiles,"
                    + "ProductionJavaPaths,"
                    + "Subject"
            );

            writer.newLine();

            for (FixCommitCandidate candidate
                    : candidates) {

                writer.write(
                    csv(candidate.issueKey())
                );

                writer.write(",");
                writer.write(
                    csv(candidate.commitId())
                );

                writer.write(",");
                writer.write(
                    csv(
                        candidate.commitDate()
                            .toString()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        candidate.parentCount()
                    )
                );

                writer.write(",");
                writer.write(
                    Boolean.toString(
                        candidate.merge()
                    )
                );

                writer.write(",");
                writer.write(
                    Boolean.toString(
                        candidate.revert()
                    )
                );

                writer.write(",");
                writer.write(
                    Boolean.toString(
                        candidate.issueInSubject()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        candidate.changedFiles()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        candidate
                            .productionJavaFiles()
                            .size()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        String.join(
                            " | ",
                            candidate
                                .productionJavaFiles()
                        )
                    )
                );

                writer.write(",");
                writer.write(
                    csv(candidate.subject())
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