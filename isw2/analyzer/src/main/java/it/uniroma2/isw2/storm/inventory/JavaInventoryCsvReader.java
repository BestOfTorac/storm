package it.uniroma2.isw2.storm.inventory;

import it.uniroma2.isw2.storm.model.JavaFileInventoryEntry;
import it.uniroma2.isw2.storm.model.SourceCategory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JavaInventoryCsvReader {

    private static final String EXPECTED_HEADER =
        "ReleaseIndex,Version,CommitId,FilePath,SourceCategory";

    private static final int COLUMN_COUNT = 5;

    private JavaInventoryCsvReader() {
        // Utility class.
    }

    public static List<JavaFileInventoryEntry> read(
            Path inputPath)
            throws IOException {

        if (!Files.isRegularFile(inputPath)) {
            throw new IOException(
                "Java inventory not found: "
                    + inputPath
            );
        }

        List<JavaFileInventoryEntry> entries =
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
                    "Unexpected Java inventory header."
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
                        new JavaFileInventoryEntry(
                            Integer.parseInt(
                                columns.get(0)
                            ),
                            columns.get(1),
                            columns.get(2),
                            columns.get(3),
                            SourceCategory.valueOf(
                                columns.get(4)
                            )
                        )
                    );

                } catch (
                        IllegalArgumentException exception) {

                    throw new IOException(
                        "Invalid Java inventory value "
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