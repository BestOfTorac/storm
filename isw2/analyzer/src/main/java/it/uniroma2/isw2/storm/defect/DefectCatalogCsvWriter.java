package it.uniroma2.isw2.storm.defect;

import it.uniroma2.isw2.storm.model.JiraDefectInfo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DefectCatalogCsvWriter {

    private static final String VERSION_SEPARATOR =
        " | ";

    private DefectCatalogCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<JiraDefectInfo> defects)
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
                "IssueId,"
                    + "IssueKey,"
                    + "IssueType,"
                    + "Status,"
                    + "Resolution,"
                    + "CreatedAt,"
                    + "ResolvedAt,"
                    + "AffectedVersions,"
                    + "FixVersions,"
                    + "Summary"
            );

            writer.newLine();

            for (JiraDefectInfo defect
                    : defects) {

                writer.write(
                    csv(defect.id())
                );

                writer.write(",");
                writer.write(
                    csv(defect.key())
                );

                writer.write(",");
                writer.write(
                    csv(defect.issueType())
                );

                writer.write(",");
                writer.write(
                    csv(defect.status())
                );

                writer.write(",");
                writer.write(
                    csv(defect.resolution())
                );

                writer.write(",");
                writer.write(
                    csv(
                        defect.createdAt()
                            .toString()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        defect.resolvedAt() == null
                            ? ""
                            : defect.resolvedAt()
                                .toString()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        String.join(
                            VERSION_SEPARATOR,
                            defect
                                .affectedVersions()
                        )
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        String.join(
                            VERSION_SEPARATOR,
                            defect
                                .fixVersions()
                        )
                    )
                );

                writer.write(",");
                writer.write(
                    csv(defect.summary())
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