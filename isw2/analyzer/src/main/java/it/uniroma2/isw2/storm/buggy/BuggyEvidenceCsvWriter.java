package it.uniroma2.isw2.storm.buggy;

import it.uniroma2.isw2.storm.model.BuggyEvidenceEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BuggyEvidenceCsvWriter {

    private BuggyEvidenceCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<BuggyEvidenceEntry> rows)
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
                "IssueKey,ReleaseIndex,Version,"
                    + "CommitId,FilePath,MappingStrategy,"
                    + "EffectiveIV,FV,IVSource,FVSource,"
                    + "BlamedFilePath,FixedFilePath"
            );

            writer.newLine();

            for (BuggyEvidenceEntry row
                    : rows) {

                writer.write(
                    escape(row.issueKey())
                );

                writer.write(',');
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
                    row.mappingStrategy()
                        .name()
                );

                writer.write(',');
                writer.write(
                    Integer.toString(
                        row.effectiveIntroductionVersion()
                    )
                );

                writer.write(',');
                writer.write(
                    Integer.toString(
                        row.fixVersion()
                    )
                );

                writer.write(',');
                writer.write(
                    escape(
                        row.introductionVersionSource()
                    )
                );

                writer.write(',');
                writer.write(
                    escape(
                        row.fixVersionSource()
                    )
                );

                writer.write(',');
                writer.write(
                    escape(
                        row.blamedFilePath()
                    )
                );

                writer.write(',');
                writer.write(
                    escape(
                        row.fixedFilePath()
                    )
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