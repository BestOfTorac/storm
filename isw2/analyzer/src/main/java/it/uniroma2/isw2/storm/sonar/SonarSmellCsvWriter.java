package it.uniroma2.isw2.storm.sonar;

import it.uniroma2.isw2.storm.model.SonarSmellMetric;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SonarSmellCsvWriter {

    private SonarSmellCsvWriter() {
        // Utility class.
    }

    public static void write(
            Path output,
            List<SonarSmellMetric> rows)
            throws IOException {

        Files.createDirectories(output.getParent());

        try (BufferedWriter writer =
                Files.newBufferedWriter(
                    output,
                    StandardCharsets.UTF_8
                )) {

            writer.write(
                "ReleaseIndex,Version,CommitId,"
                    + "FilePath,NSMELLS,SonarAnalysisStatus"
            );
            writer.newLine();

            for (SonarSmellMetric row : rows) {
                writer.write(
                    csv(Integer.toString(row.releaseIndex()))
                );
                writer.write(',');
                writer.write(csv(row.version()));
                writer.write(',');
                writer.write(csv(row.commitId()));
                writer.write(',');
                writer.write(csv(row.filePath()));
                writer.write(',');
                writer.write(
                    Integer.toString(row.nSmells())
                );
                writer.write(',');
                writer.write(
                    csv(row.analysisStatus())
                );
                writer.newLine();
            }
        }
    }

    private static String csv(String value) {
        String normalized =
            value == null ? "" : value;

        boolean quote =
            normalized.indexOf(',') >= 0
                || normalized.indexOf('"') >= 0
                || normalized.indexOf('\n') >= 0
                || normalized.indexOf('\r') >= 0;

        if (!quote) {
            return normalized;
        }

        return '"'
            + normalized.replace("\"", "\"\"")
            + '"';
    }
}