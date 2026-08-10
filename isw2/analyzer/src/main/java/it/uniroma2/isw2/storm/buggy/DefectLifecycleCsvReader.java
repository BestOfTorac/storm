package it.uniroma2.isw2.storm.buggy;

import it.uniroma2.isw2.storm.model.DefectLifecycle;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DefectLifecycleCsvReader {

    private static final String EXPECTED_HEADER =
        "IssueKey,ObservedIV,ObservedIVVersion,"
            + "OV,OVVersion,FV,FVVersion,FVSource,"
            + "IVSource,LAV,EffectiveIV,PUsed,"
            + "OverlapsDataset,SzzFixCommits,SzzRows,"
            + "JiraAffectedVersions,JiraFixVersions";

    private static final int COLUMN_COUNT = 17;

    private DefectLifecycleCsvReader() {
        // Utility class.
    }

    public static List<DefectLifecycle> read(
            Path inputPath)
            throws IOException {

        if (!Files.isRegularFile(inputPath)) {
            throw new IOException(
                "Defect lifecycle not found: "
                    + inputPath
            );
        }

        List<DefectLifecycle> entries =
            new ArrayList<>();

        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    inputPath,
                    StandardCharsets.UTF_8
                )
        ) {
            String header =
                reader.readLine();

            if (
                header != null
                    && header.startsWith("\uFEFF")
            ) {
                header =
                    header.substring(1);
            }

            if (!EXPECTED_HEADER.equals(header)) {
                throw new IOException(
                    "Unexpected defect lifecycle header."
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                List<String> columns =
                    parseCsvLine(
                        line,
                        lineNumber
                    );

                if (columns.size() != COLUMN_COUNT) {
                    throw new IOException(
                        "Expected "
                            + COLUMN_COUNT
                            + " columns at line "
                            + lineNumber
                            + ", found "
                            + columns.size()
                            + "."
                    );
                }

                try {
                    entries.add(
                        new DefectLifecycle(
                            columns.get(0),
                            nullableInteger(
                                columns.get(1)
                            ),
                            columns.get(2),
                            Integer.parseInt(
                                columns.get(3)
                            ),
                            columns.get(4),
                            Integer.parseInt(
                                columns.get(5)
                            ),
                            columns.get(6),
                            DefectLifecycle
                                .FixVersionSource
                                .valueOf(
                                    columns.get(7)
                                ),
                            DefectLifecycle
                                .IntroductionVersionSource
                                .valueOf(
                                    columns.get(8)
                                ),
                            nullableDouble(
                                columns.get(9)
                            ),
                            Integer.parseInt(
                                columns.get(10)
                            ),
                            nullableDouble(
                                columns.get(11)
                            ),
                            parseBoolean(
                                columns.get(12),
                                lineNumber
                            ),
                            Integer.parseInt(
                                columns.get(13)
                            ),
                            Integer.parseInt(
                                columns.get(14)
                            ),
                            columns.get(15),
                            columns.get(16)
                        )
                    );

                } catch (
                        IllegalArgumentException exception) {

                    throw new IOException(
                        "Invalid defect lifecycle value "
                            + "at line "
                            + lineNumber
                            + ".",
                        exception
                    );
                }
            }
        }

        return List.copyOf(entries);
    }

    private static Integer nullableInteger(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Integer.valueOf(value);
    }

    private static Double nullableDouble(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return Double.valueOf(value);
    }

    private static boolean parseBoolean(
            String value,
            int lineNumber)
            throws IOException {

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new IOException(
            "Invalid boolean at line "
                + lineNumber
                + ": "
                + value
        );
    }

    private static List<String> parseCsvLine(
            String line,
            int lineNumber)
            throws IOException {

        List<String> fields =
            new ArrayList<>();

        StringBuilder current =
            new StringBuilder();

        boolean insideQuotes = false;

        for (
            int index = 0;
            index < line.length();
            index++
        ) {
            char character =
                line.charAt(index);

            if (insideQuotes) {

                if (character == '"') {

                    boolean escapedQuote =
                        index + 1 < line.length()
                            && line.charAt(index + 1)
                                == '"';

                    if (escapedQuote) {
                        current.append('"');
                        index++;
                    } else {
                        insideQuotes = false;
                    }

                } else {
                    current.append(character);
                }

            } else if (character == ',') {

                fields.add(
                    current.toString()
                );

                current.setLength(0);

            } else if (character == '"') {

                insideQuotes = true;

            } else {

                current.append(character);
            }
        }

        if (insideQuotes) {
            throw new IOException(
                "Unclosed quoted field at line "
                    + lineNumber
                    + "."
            );
        }

        fields.add(
            current.toString()
        );

        return fields;
    }
}