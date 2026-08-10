package it.uniroma2.isw2.storm.defect;

import it.uniroma2.isw2.storm.model.JiraDefectInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class DefectCatalogCsvReader {

    private static final String EXPECTED_HEADER =
        "IssueId,IssueKey,IssueType,Status,Resolution,"
            + "CreatedAt,ResolvedAt,AffectedVersions,"
            + "FixVersions,Summary";

    private static final int COLUMN_COUNT = 10;

    private DefectCatalogCsvReader() {
        // Utility class.
    }

    public static List<JiraDefectInfo> read(
            Path csvPath)
            throws IOException {

        if (!Files.isRegularFile(csvPath)) {
            throw new IOException(
                "Defect catalog not found: "
                    + csvPath
            );
        }

        List<JiraDefectInfo> defects =
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

            if (
                header != null
                    && header.startsWith("\uFEFF")
            ) {
                header =
                    header.substring(1);
            }

            if (!EXPECTED_HEADER.equals(header)) {
                throw new IOException(
                    "Unexpected defect catalog header."
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
                    parseCsvLine(
                        line,
                        lineNumber
                    );

                if (
                    columns.size()
                        != COLUMN_COUNT
                ) {
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
                    defects.add(
                        new JiraDefectInfo(
                            columns.get(0),
                            columns.get(1),
                            columns.get(2),
                            columns.get(3),
                            columns.get(4),
                            OffsetDateTime.parse(
                                columns.get(5)
                            ),
                            parseOptionalDateTime(
                                columns.get(6)
                            ),
                            parseVersions(
                                columns.get(7)
                            ),
                            parseVersions(
                                columns.get(8)
                            ),
                            columns.get(9)
                        )
                    );

                } catch (
                        DateTimeParseException exception) {

                    throw new IOException(
                        "Invalid defect catalog date "
                            + "at line "
                            + lineNumber
                            + ".",
                        exception
                    );
                }
            }
        }

        return List.copyOf(
            defects
        );
    }

    private static OffsetDateTime
            parseOptionalDateTime(
                String value) {

        if (
            value == null
                || value.isBlank()
        ) {
            return null;
        }

        return OffsetDateTime.parse(
            value
        );
    }

    private static List<String> parseVersions(
            String value) {

        if (
            value == null
                || value.isBlank()
        ) {
            return List.of();
        }

        String[] rawVersions =
            value.split(
                "\\s*\\|\\s*"
            );

        List<String> versions =
            new ArrayList<>();

        for (String rawVersion
                : rawVersions) {

            String version =
                rawVersion.trim();

            if (!version.isBlank()) {
                versions.add(version);
            }
        }

        return List.copyOf(
            versions
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
                        index + 1
                            < line.length()
                            && line.charAt(
                                index + 1
                            ) == '"';

                    if (escapedQuote) {

                        current.append('"');
                        index++;

                    } else {

                        insideQuotes = false;
                    }

                } else {

                    current.append(
                        character
                    );
                }

            } else if (
                character == ','
            ) {

                fields.add(
                    current.toString()
                );

                current.setLength(0);

            } else if (
                character == '"'
            ) {

                insideQuotes = true;

            } else {

                current.append(
                    character
                );
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