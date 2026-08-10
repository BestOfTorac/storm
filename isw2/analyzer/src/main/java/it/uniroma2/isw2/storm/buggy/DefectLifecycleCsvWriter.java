package it.uniroma2.isw2.storm.buggy;

import it.uniroma2.isw2.storm.model.DefectLifecycle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DefectLifecycleCsvWriter {

    private DefectLifecycleCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path outputPath,
            List<DefectLifecycle> lifecycles)
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
                    + "ObservedIV,"
                    + "ObservedIVVersion,"
                    + "OV,"
                    + "OVVersion,"
                    + "FV,"
                    + "FVVersion,"
                    + "FVSource,"
                    + "IVSource,"
                    + "LAV,"
                    + "EffectiveIV,"
                    + "PUsed,"
                    + "OverlapsDataset,"
                    + "SzzFixCommits,"
                    + "SzzRows,"
                    + "JiraAffectedVersions,"
                    + "JiraFixVersions"
            );

            writer.newLine();

            for (DefectLifecycle lifecycle
                    : lifecycles) {

                writer.write(
                    csv(lifecycle.issueKey())
                );

                writer.write(",");
                writer.write(
                    lifecycle
                            .observedIntroductionVersion()
                        == null
                            ? ""
                            : Integer.toString(
                                lifecycle
                                    .observedIntroductionVersion()
                            )
                );

                writer.write(",");
                writer.write(
                    csv(
                        lifecycle
                            .observedIntroductionVersionName()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        lifecycle.openingVersion()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        lifecycle.openingVersionName()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        lifecycle.fixVersion()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        lifecycle.fixVersionName()
                    )
                );

                writer.write(",");
                writer.write(
                    lifecycle
                        .fixVersionSource()
                        .name()
                );

                writer.write(",");
                writer.write(
                    lifecycle
                        .introductionVersionSource()
                        .name()
                );

                writer.write(",");
                writer.write(
                    lifecycle.estimatedLav() == null
                        ? ""
                        : Double.toString(
                            lifecycle.estimatedLav()
                        )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        lifecycle
                            .effectiveIntroductionVersion()
                    )
                );

                writer.write(",");
                writer.write(
                    lifecycle.proportionUsed() == null
                        ? ""
                        : Double.toString(
                            lifecycle.proportionUsed()
                        )
                );

                writer.write(",");
                writer.write(
                    Boolean.toString(
                        lifecycle.overlapsDataset()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        lifecycle.szzFixCommits()
                    )
                );

                writer.write(",");
                writer.write(
                    Integer.toString(
                        lifecycle.szzRows()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        lifecycle
                            .jiraAffectedVersions()
                    )
                );

                writer.write(",");
                writer.write(
                    csv(
                        lifecycle
                            .jiraFixVersions()
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