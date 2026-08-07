package it.uniroma2.isw2.storm.defect;

import it.uniroma2.isw2.storm.model.FixCommitCandidate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class FixCommitCandidateCsvReader {

    private static final int COLUMN_COUNT = 11;

    private FixCommitCandidateCsvReader() {
        // Utility class.
    }

    public static List<FixCommitCandidate> read(
            Path csvPath)
            throws IOException {

        List<FixCommitCandidate> candidates =
            new ArrayList<>();

        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    csvPath,
                    StandardCharsets.UTF_8
                )
        ) {
            String header =
                reader.readLine();

            if (header == null) {
                throw new IOException(
                    "Fix candidate CSV is empty."
                );
            }

            String line;
            int lineNumber = 1;

            while (
                (line = reader.readLine()) != null
            ) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> columns =
                    parseCsvLine(line);

                if (columns.size() != COLUMN_COUNT) {
                    throw new IOException(
                        "Invalid fix candidate CSV "
                            + "at line "
                            + lineNumber
                            + ": expected "
                            + COLUMN_COUNT
                            + " columns but found "
                            + columns.size()
                    );
                }

                try {
                    List<String> productionPaths =
                        parsePaths(
                            columns.get(9)
                        );

                    int declaredProductionCount =
                        Integer.parseInt(
                            columns.get(8)
                        );

                    if (
                        declaredProductionCount
                            != productionPaths.size()
                    ) {
                        throw new IOException(
                            "Production Java count mismatch "
                                + "at line "
                                + lineNumber
                        );
                    }

                    candidates.add(
                        new FixCommitCandidate(
                            columns.get(0),
                            columns.get(1),
                            OffsetDateTime.parse(
                                columns.get(2)
                            ),
                            Integer.parseInt(
                                columns.get(3)
                            ),
                            Boolean.parseBoolean(
                                columns.get(4)
                            ),
                            Boolean.parseBoolean(
                                columns.get(5)
                            ),
                            Boolean.parseBoolean(
                                columns.get(6)
                            ),
                            Integer.parseInt(
                                columns.get(7)
                            ),
                            productionPaths,
                            columns.get(10)
                        )
                    );

                } catch (
                        NumberFormatException
                        | java.time.DateTimeException exception) {

                    throw new IOException(
                        "Unable to parse fix candidate CSV "
                            + "at line "
                            + lineNumber,
                        exception
                    );
                }
            }
        }

        return List.copyOf(
            candidates
        );
    }

    private static List<String> parsePaths(
            String value) {

        if (
            value == null
                || value.isBlank()
        ) {
            return List.of();
        }

        return List.of(
            value.split(
                "\\s+\\|\\s+"
            )
        );
    }

    private static List<String> parseCsvLine(
            String line)
            throws IOException {

        List<String> fields =
            new ArrayList<>();

        StringBuilder current =
            new StringBuilder();

        boolean quoted = false;

        for (
            int index = 0;
            index < line.length();
            index++
        ) {
            char character =
                line.charAt(index);

            if (quoted) {

                if (character == '"') {

                    if (
                        index + 1 < line.length()
                            && line.charAt(index + 1)
                                == '"'
                    ) {
                        current.append('"');
                        index++;

                    } else {
                        quoted = false;
                    }

                } else {
                    current.append(
                        character
                    );
                }

            } else {

                if (character == ',') {

                    fields.add(
                        current.toString()
                    );

                    current.setLength(0);

                } else if (character == '"') {

                    quoted = true;

                } else {

                    current.append(
                        character
                    );
                }
            }
        }

        if (quoted) {
            throw new IOException(
                "Unterminated quoted CSV field."
            );
        }

        fields.add(
            current.toString()
        );

        return fields;
    }
}