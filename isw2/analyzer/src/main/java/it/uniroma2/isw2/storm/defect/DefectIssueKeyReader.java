package it.uniroma2.isw2.storm.defect;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DefectIssueKeyReader {

    private DefectIssueKeyReader() {
        // Utility class.
    }

    public static Set<String> read(
            Path csvPath)
            throws IOException {

        List<String> lines =
            Files.readAllLines(
                csvPath,
                StandardCharsets.UTF_8
            );

        Set<String> issueKeys =
            new HashSet<>();

        for (int index = 1;
                index < lines.size();
                index++) {

            String line =
                lines.get(index);

            if (line.isBlank()) {
                continue;
            }

            /*
             * IssueId and IssueKey are the first two fields and
             * cannot contain commas.
             *
             * The remaining CSV fields may contain quoted commas,
             * therefore the split is deliberately limited to 3.
             */
            String[] columns =
                line.split(
                    ",",
                    3
                );

            if (columns.length < 2) {
                continue;
            }

            String issueKey =
                columns[1]
                    .trim()
                    .toUpperCase(
                        Locale.ROOT
                    );

            if (
                issueKey.matches(
                    "STORM-\\d+"
                )
            ) {
                issueKeys.add(
                    issueKey
                );
            }
        }

        return Set.copyOf(
            issueKeys
        );
    }
}