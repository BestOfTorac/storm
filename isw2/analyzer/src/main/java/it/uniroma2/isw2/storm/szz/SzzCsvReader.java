package it.uniroma2.isw2.storm.szz;

import it.uniroma2.isw2.storm.model.FixCommitSelectionStrategy;
import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SzzCsvReader {

    private static final String EXPECTED_HEADER =
        "IssueKey,"
            + "FixCommitId,"
            + "FixSelectionStrategy,"
            + "ParentCommitId,"
            + "FixedFilePath,"
            + "BlamedFilePath,"
            + "BugIntroducingCommitId,"
            + "BlamedLineCount";

    private static final int COLUMN_COUNT = 8;

    private SzzCsvReader() {
        // Utility class.
    }

    public static List<SzzBugIntroducingChange> read(
            Path csvPath)
            throws IOException {

        if (!Files.isRegularFile(csvPath)) {
            throw new IOException(
                "SZZ dataset not found: "
                    + csvPath
            );
        }

        List<SzzBugIntroducingChange> rows =
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
                    "Unexpected SZZ CSV header."
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
                            + " SZZ columns at line "
                            + lineNumber
                            + ", found "
                            + columns.size()
                            + "."
                    );
                }

                try {
                    int blamedLineCount =
                        Integer.parseInt(
                            columns.get(7)
                        );

                    if (blamedLineCount <= 0) {
                        throw new IOException(
                            "Invalid blamed line count "
                                + "at line "
                                + lineNumber
                                + "."
                        );
                    }

                    rows.add(
                        new SzzBugIntroducingChange(
                            columns.get(0),
                            columns.get(1),
                            FixCommitSelectionStrategy.valueOf(
                                columns.get(2)
                            ),
                            columns.get(3),
                            columns.get(4),
                            columns.get(5),
                            columns.get(6),
                            blamedLineCount
                        )
                    );

                } catch (
                        IllegalArgumentException exception) {

                    throw new IOException(
                        "Unable to parse SZZ row "
                            + "at line "
                            + lineNumber
                            + ".",
                        exception
                    );
                }
            }
        }

        return List.copyOf(rows);
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