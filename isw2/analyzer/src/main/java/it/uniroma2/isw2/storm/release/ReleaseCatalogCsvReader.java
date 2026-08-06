package it.uniroma2.isw2.storm.release;

import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class ReleaseCatalogCsvReader {

    private static final String EXPECTED_HEADER =
        "Index,Version,ReleaseDate,"
            + "ReleaseDateSource,"
            + "JiraVersionId,"
            + "GitHubTag,"
            + "GitTag,"
            + "GitCommitId,"
            + "GitCommitDate,"
            + "DatasetIncluded";

    private static final int COLUMN_COUNT = 10;

    private ReleaseCatalogCsvReader() {
        // Utility class.
    }

    public static List<ReleaseCatalogEntry> read(
            Path inputPath)
            throws IOException {

        if (!Files.isRegularFile(inputPath)) {
            throw new IOException(
                "Release catalog not found: "
                    + inputPath
            );
        }

        List<ReleaseCatalogEntry> entries =
            new ArrayList<>();

        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    inputPath,
                    StandardCharsets.UTF_8
                )
        ) {
            String header = reader.readLine();

            if (
                header != null
                    && header.startsWith("\uFEFF")
            ) {
                header = header.substring(1);
            }

            if (!EXPECTED_HEADER.equals(header)) {
                throw new IOException(
                    "Unexpected release catalog header."
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

                entries.add(
                    parseEntry(
                        columns,
                        lineNumber
                    )
                );
            }
        }

        return List.copyOf(entries);
    }

    private static ReleaseCatalogEntry parseEntry(
            List<String> columns,
            int lineNumber)
            throws IOException {

        try {
            return new ReleaseCatalogEntry(
                Integer.parseInt(columns.get(0)),
                columns.get(1),
                LocalDate.parse(columns.get(2)),
                columns.get(3),
                columns.get(4),
                columns.get(5),
                columns.get(6),
                columns.get(7),
                parseOptionalDate(columns.get(8)),
                parseBoolean(
                    columns.get(9),
                    lineNumber
                )
            );

        } catch (
                NumberFormatException
                | DateTimeParseException exception) {

            throw new IOException(
                "Invalid release catalog value "
                    + "at line "
                    + lineNumber
                    + ".",
                exception
            );
        }
    }

    private static LocalDate parseOptionalDate(
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
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
            "Invalid boolean value at line "
                + lineNumber
                + ": "
                + value
        );
    }

    private static List<String> parseCsvLine(
            String line,
            int lineNumber)
            throws IOException {

        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;

        for (int index = 0;
                index < line.length();
                index++) {

            char character = line.charAt(index);

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
                fields.add(current.toString());
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

        fields.add(current.toString());

        return fields;
    }
}