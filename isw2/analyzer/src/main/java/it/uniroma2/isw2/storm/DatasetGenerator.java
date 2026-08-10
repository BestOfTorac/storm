package it.uniroma2.isw2.storm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DatasetGenerator {

    private static final List<String> INVENTORY_HEADER =
        List.of(
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath",
            "SourceCategory"
        );

    private static final List<String> LOC_HEADER =
        List.of(
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath",
            "LOC"
        );

    private static final List<String> HISTORICAL_HEADER =
        List.of(
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath",
            "LOC_TOUCHED",
            "NR",
            "NAUTH",
            "LOC_ADDED",
            "MAX_LOC_ADDED",
            "AVG_LOC_ADDED",
            "LOC_DELETED",
            "MAX_LOC_DELETED",
            "AVG_LOC_DELETED",
            "CHURN",
            "MAX_CHURN",
            "AVG_CHURN",
            "CHANGE_SET_SIZE",
            "MAX_CHANGE_SET",
            "AVG_CHANGE_SET",
            "AVG_ND",
            "MAX_ND",
            "AVG_ENTROPY",
            "MAX_ENTROPY",
            "AGE_WEEKS",
            "WEIGHTED_AGE_WEEKS"
        );

    private static final List<String> NFIX_HEADER =
        List.of(
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath",
            "NFIX"
        );

    private static final List<String> SMELL_HEADER =
        List.of(
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath",
            "NSMELLS"
        );

    private static final List<String> BUGGY_HEADER =
        List.of(
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath",
            "BUGGY"
        );

    /*
     * Required professor metrics are kept first.
     *
     * The seven additional raw metrics follow them.
     *
     * BUGGY is deliberately the last column.
     */
    private static final List<String> OUTPUT_HEADER =
        List.of(
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath",

            "LOC",
            "LOC_TOUCHED",
            "NR",
            "NFIX",
            "NAUTH",

            "LOC_ADDED",
            "MAX_LOC_ADDED",
            "AVG_LOC_ADDED",

            "CHURN",
            "MAX_CHURN",
            "AVG_CHURN",

            "CHANGE_SET_SIZE",
            "MAX_CHANGE_SET_SIZE",
            "AVG_CHANGE_SET_SIZE",

            "AGE_WEEKS",
            "WEIGHTED_AGE_WEEKS",

            "LOC_DELETED",
            "MAX_LOC_DELETED",
            "AVG_LOC_DELETED",

            "AVG_ND",
            "MAX_ND",

            "AVG_ENTROPY",
            "MAX_ENTROPY",

            "NSMELLS",

            "BUGGY"
        );

    private DatasetGenerator() {
        // Application entry point.
    }

    public static void main(String[] args) {

        int exitCode =
            run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {

        if (args.length != 1) {

            System.err.println(
                "Usage: DatasetGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path datasets =
            repositoryPath.resolve(
                "isw2/datasets"
            );

        Path inventoryPath =
            datasets.resolve(
                "java_class_inventory.csv"
            );

        Path locPath =
            datasets.resolve(
                "loc_metrics.csv"
            );

        Path historicalPath =
            datasets.resolve(
                "historical_metrics.csv"
            );

        Path nfixPath =
            datasets.resolve(
                "nfix_metrics.csv"
            );

        Path smellPath =
            datasets.resolve(
                "smell_metrics.csv"
            );

        Path buggyPath =
            datasets.resolve(
                "buggy_labels.csv"
            );

        Path outputPath =
            datasets.resolve(
                "storm_m1_dataset.csv"
            );

        try {

            CsvTable inventory =
                readCsv(
                    inventoryPath,
                    INVENTORY_HEADER
                );

            CsvTable loc =
                readCsv(
                    locPath,
                    LOC_HEADER
                );

            CsvTable historical =
                readCsv(
                    historicalPath,
                    HISTORICAL_HEADER
                );

            CsvTable nfix =
                readCsv(
                    nfixPath,
                    NFIX_HEADER
                );

            CsvTable smells =
                readCsv(
                    smellPath,
                    SMELL_HEADER
                );

            CsvTable buggy =
                readCsv(
                    buggyPath,
                    BUGGY_HEADER
                );


            // ------------------------------------------------
            // Canonical production observations.
            // ------------------------------------------------

            List<CsvRow> production =
                inventory.rows()
                    .stream()
                    .filter(
                        row ->
                            "PRODUCTION".equals(
                                row.get(
                                    "SourceCategory"
                                )
                            )
                    )
                    .sorted(
                        Comparator
                            .comparingInt(
                                (CsvRow row) ->
                                    Integer.parseInt(
                                        row.get(
                                            "ReleaseIndex"
                                        )
                                    )
                            )
                            .thenComparing(
                                (CsvRow row) ->
                                    row.get(
                                        "FilePath"
                                    )
                            )
                    )
                    .toList();


            Map<ObservationKey, CsvRow>
                productionByKey =
                    indexRows(
                        production,
                        "production inventory"
                    );


            // ------------------------------------------------
            // Every metric dataset must match exactly the
            // production inventory.
            // ------------------------------------------------

            Map<ObservationKey, CsvRow> locByKey =
                validateMetricTable(
                    "LOC",
                    loc,
                    productionByKey
                );

            Map<ObservationKey, CsvRow>
                historicalByKey =
                    validateMetricTable(
                        "historical metrics",
                        historical,
                        productionByKey
                    );

            Map<ObservationKey, CsvRow> nfixByKey =
                validateMetricTable(
                    "NFIX",
                    nfix,
                    productionByKey
                );

            Map<ObservationKey, CsvRow> smellsByKey =
                validateMetricTable(
                    "NSMELLS",
                    smells,
                    productionByKey
                );

            Map<ObservationKey, CsvRow> buggyByKey =
                validateMetricTable(
                    "BUGGY",
                    buggy,
                    productionByKey
                );


            List<OutputRow> outputRows =
                new ArrayList<>();


            long buggyYes = 0;
            long buggyNo = 0;

            long withSmells = 0;
            long withoutSmells = 0;

            long buggyWithSmells = 0;
            long buggyWithoutSmells = 0;

            long cleanWithSmells = 0;
            long cleanWithoutSmells = 0;


            Map<Integer, ReleaseSummary>
                summaryByRelease =
                    new LinkedHashMap<>();


            for (CsvRow inventoryRow
                    : production) {

                int releaseIndex =
                    parseInteger(
                        inventoryRow.get(
                            "ReleaseIndex"
                        ),
                        "ReleaseIndex"
                    );

                String version =
                    required(
                        inventoryRow,
                        "Version"
                    );

                String commitId =
                    required(
                        inventoryRow,
                        "CommitId"
                    );

                String filePath =
                    required(
                        inventoryRow,
                        "FilePath"
                    );


                ObservationKey key =
                    new ObservationKey(
                        releaseIndex,
                        filePath
                    );


                CsvRow locRow =
                    locByKey.get(key);

                CsvRow historicalRow =
                    historicalByKey.get(key);

                CsvRow nfixRow =
                    nfixByKey.get(key);

                CsvRow smellRow =
                    smellsByKey.get(key);

                CsvRow buggyRow =
                    buggyByKey.get(key);


                validateIdentity(
                    key,
                    inventoryRow,
                    locRow,
                    "LOC"
                );

                validateIdentity(
                    key,
                    inventoryRow,
                    historicalRow,
                    "historical metrics"
                );

                validateIdentity(
                    key,
                    inventoryRow,
                    nfixRow,
                    "NFIX"
                );

                validateIdentity(
                    key,
                    inventoryRow,
                    smellRow,
                    "NSMELLS"
                );

                validateIdentity(
                    key,
                    inventoryRow,
                    buggyRow,
                    "BUGGY"
                );


                int locValue =
                    parseNonNegativeInteger(
                        required(
                            locRow,
                            "LOC"
                        ),
                        "LOC",
                        key
                    );

                int nfixValue =
                    parseNonNegativeInteger(
                        required(
                            nfixRow,
                            "NFIX"
                        ),
                        "NFIX",
                        key
                    );

                int nSmells =
                    parseNonNegativeInteger(
                        required(
                            smellRow,
                            "NSMELLS"
                        ),
                        "NSMELLS",
                        key
                    );

                String buggyValue =
                    required(
                        buggyRow,
                        "BUGGY"
                    );

                if (
                    !"YES".equals(buggyValue)
                        && !"NO".equals(buggyValue)
                ) {

                    throw new IOException(
                        "Invalid BUGGY value for "
                            + key
                            + ": "
                            + buggyValue
                    );
                }


                /*
                 * Historical values are copied exactly as
                 * generated by the historical analyzer.
                 *
                 * We still require every value to be present.
                 */
                requireHistoricalMetrics(
                    historicalRow,
                    key
                );


                OutputRow outputRow =
                    new OutputRow(
                        List.of(
                            Integer.toString(
                                releaseIndex
                            ),
                            version,
                            commitId,
                            filePath,

                            Integer.toString(
                                locValue
                            ),

                            historicalRow.get(
                                "LOC_TOUCHED"
                            ),

                            historicalRow.get(
                                "NR"
                            ),

                            Integer.toString(
                                nfixValue
                            ),

                            historicalRow.get(
                                "NAUTH"
                            ),

                            historicalRow.get(
                                "LOC_ADDED"
                            ),

                            historicalRow.get(
                                "MAX_LOC_ADDED"
                            ),

                            historicalRow.get(
                                "AVG_LOC_ADDED"
                            ),

                            historicalRow.get(
                                "CHURN"
                            ),

                            historicalRow.get(
                                "MAX_CHURN"
                            ),

                            historicalRow.get(
                                "AVG_CHURN"
                            ),

                            historicalRow.get(
                                "CHANGE_SET_SIZE"
                            ),

                            historicalRow.get(
                                "MAX_CHANGE_SET"
                            ),

                            historicalRow.get(
                                "AVG_CHANGE_SET"
                            ),

                            historicalRow.get(
                                "AGE_WEEKS"
                            ),

                            historicalRow.get(
                                "WEIGHTED_AGE_WEEKS"
                            ),

                            historicalRow.get(
                                "LOC_DELETED"
                            ),

                            historicalRow.get(
                                "MAX_LOC_DELETED"
                            ),

                            historicalRow.get(
                                "AVG_LOC_DELETED"
                            ),

                            historicalRow.get(
                                "AVG_ND"
                            ),

                            historicalRow.get(
                                "MAX_ND"
                            ),

                            historicalRow.get(
                                "AVG_ENTROPY"
                            ),

                            historicalRow.get(
                                "MAX_ENTROPY"
                            ),

                            Integer.toString(
                                nSmells
                            ),

                            buggyValue
                        )
                    );


                if (
                    outputRow.values().size()
                        != OUTPUT_HEADER.size()
                ) {

                    throw new IOException(
                        "Output column mismatch for "
                            + key
                    );
                }


                outputRows.add(
                    outputRow
                );


                boolean isBuggy =
                    "YES".equals(
                        buggyValue
                    );

                boolean hasSmells =
                    nSmells > 0;


                if (isBuggy) {
                    buggyYes++;
                } else {
                    buggyNo++;
                }


                if (hasSmells) {
                    withSmells++;
                } else {
                    withoutSmells++;
                }


                if (
                    isBuggy
                        && hasSmells
                ) {
                    buggyWithSmells++;

                } else if (
                    isBuggy
                ) {
                    buggyWithoutSmells++;

                } else if (
                    hasSmells
                ) {
                    cleanWithSmells++;

                } else {
                    cleanWithoutSmells++;
                }


                ReleaseSummary summary =
                    summaryByRelease
                        .computeIfAbsent(
                            releaseIndex,
                            ignored ->
                                new ReleaseSummary(
                                    releaseIndex,
                                    version
                                )
                        );

                summary.add(
                    isBuggy,
                    hasSmells
                );
            }


            // ------------------------------------------------
            // Global invariants.
            // ------------------------------------------------

            if (
                outputRows.size()
                    != production.size()
            ) {

                throw new IOException(
                    "Final dataset row count mismatch."
                );
            }


            if (
                buggyYes + buggyNo
                    != outputRows.size()
            ) {

                throw new IOException(
                    "BUGGY accounting mismatch."
                );
            }


            if (
                withSmells + withoutSmells
                    != outputRows.size()
            ) {

                throw new IOException(
                    "NSMELLS accounting mismatch."
                );
            }


            if (
                buggyWithSmells
                    + buggyWithoutSmells
                    + cleanWithSmells
                    + cleanWithoutSmells
                    != outputRows.size()
            ) {

                throw new IOException(
                    "BUGGY/NSMELLS cross-table "
                        + "accounting mismatch."
                );
            }


            writeOutput(
                outputPath,
                outputRows
            );


            System.out.printf(
                "Production observations: %d%n",
                production.size()
            );

            System.out.printf(
                "Final dataset rows: %d%n",
                outputRows.size()
            );

            System.out.printf(
                "Final dataset columns: %d%n",
                OUTPUT_HEADER.size()
            );

            System.out.printf(
                "BUGGY=YES: %d%n",
                buggyYes
            );

            System.out.printf(
                "BUGGY=NO: %d%n",
                buggyNo
            );

            System.out.printf(
                "NSMELLS>0: %d%n",
                withSmells
            );

            System.out.printf(
                "NSMELLS=0: %d%n",
                withoutSmells
            );


            System.out.println();
            System.out.println(
                "BUGGY / NSMELLS:"
            );

            System.out.printf(
                "  BUGGY=YES, NSMELLS>0: %d%n",
                buggyWithSmells
            );

            System.out.printf(
                "  BUGGY=YES, NSMELLS=0: %d%n",
                buggyWithoutSmells
            );

            System.out.printf(
                "  BUGGY=NO,  NSMELLS>0: %d%n",
                cleanWithSmells
            );

            System.out.printf(
                "  BUGGY=NO,  NSMELLS=0: %d%n",
                cleanWithoutSmells
            );


            System.out.println();
            System.out.println(
                "Dataset observations by release:"
            );

            for (ReleaseSummary summary
                    : summaryByRelease.values()) {

                System.out.printf(
                    "  %2d %-18s "
                        + "classes=%4d "
                        + "buggy=%3d "
                        + "smelly=%3d%n",
                    summary.releaseIndex(),
                    summary.version(),
                    summary.total(),
                    summary.buggy(),
                    summary.smelly()
                );
            }


            System.out.printf(
                "%nFinal M1 dataset written to: %s%n",
                outputPath
            );


            return 0;

        } catch (IOException exception) {

            System.err.printf(
                "Dataset generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }


    private static Map<ObservationKey, CsvRow>
            validateMetricTable(
                String name,
                CsvTable table,
                Map<ObservationKey, CsvRow>
                    productionByKey)
            throws IOException {

        Map<ObservationKey, CsvRow> indexed =
            indexRows(
                table.rows(),
                name
            );


        Set<ObservationKey> missing =
            new HashSet<>(
                productionByKey.keySet()
            );

        missing.removeAll(
            indexed.keySet()
        );


        Set<ObservationKey> unexpected =
            new HashSet<>(
                indexed.keySet()
            );

        unexpected.removeAll(
            productionByKey.keySet()
        );


        if (
            !missing.isEmpty()
                || !unexpected.isEmpty()
        ) {

            throw new IOException(
                name
                    + " keys do not match production "
                    + "inventory. Missing="
                    + missing.size()
                    + ", unexpected="
                    + unexpected.size()
                    + "."
            );
        }


        return indexed;
    }


    private static Map<ObservationKey, CsvRow>
            indexRows(
                List<CsvRow> rows,
                String name)
            throws IOException {

        Map<ObservationKey, CsvRow> indexed =
            new HashMap<>();


        for (CsvRow row : rows) {

            int releaseIndex =
                parseInteger(
                    required(
                        row,
                        "ReleaseIndex"
                    ),
                    "ReleaseIndex"
                );

            String filePath =
                required(
                    row,
                    "FilePath"
                );


            ObservationKey key =
                new ObservationKey(
                    releaseIndex,
                    filePath
                );


            if (
                indexed.put(
                    key,
                    row
                ) != null
            ) {

                throw new IOException(
                    "Duplicate observation in "
                        + name
                        + ": "
                        + key
                );
            }
        }


        return indexed;
    }


    private static void validateIdentity(
            ObservationKey key,
            CsvRow inventory,
            CsvRow metric,
            String datasetName)
            throws IOException {

        if (metric == null) {

            throw new IOException(
                "Missing "
                    + datasetName
                    + " row for "
                    + key
            );
        }


        String expectedVersion =
            inventory.get(
                "Version"
            );

        String expectedCommit =
            inventory.get(
                "CommitId"
            );


        if (
            !expectedVersion.equals(
                metric.get(
                    "Version"
                )
            )
        ) {

            throw new IOException(
                "Version mismatch in "
                    + datasetName
                    + " for "
                    + key
            );
        }


        if (
            !expectedCommit.equals(
                metric.get(
                    "CommitId"
                )
            )
        ) {

            throw new IOException(
                "Commit mismatch in "
                    + datasetName
                    + " for "
                    + key
            );
        }
    }


    private static void requireHistoricalMetrics(
            CsvRow row,
            ObservationKey key)
            throws IOException {

        List<String> metrics =
            List.of(
                "LOC_TOUCHED",
                "NR",
                "NAUTH",

                "LOC_ADDED",
                "MAX_LOC_ADDED",
                "AVG_LOC_ADDED",

                "LOC_DELETED",
                "MAX_LOC_DELETED",
                "AVG_LOC_DELETED",

                "CHURN",
                "MAX_CHURN",
                "AVG_CHURN",

                "CHANGE_SET_SIZE",
                "MAX_CHANGE_SET",
                "AVG_CHANGE_SET",

                "AVG_ND",
                "MAX_ND",

                "AVG_ENTROPY",
                "MAX_ENTROPY",

                "AGE_WEEKS",
                "WEIGHTED_AGE_WEEKS"
            );


        for (String metric : metrics) {

            String value =
                row.get(metric);

            if (
                value == null
                    || value.isBlank()
            ) {

                throw new IOException(
                    "Missing "
                        + metric
                        + " for "
                        + key
                );
            }
        }
    }


    private static CsvTable readCsv(
            Path path,
            List<String> expectedHeader)
            throws IOException {

        if (!Files.isRegularFile(path)) {

            throw new IOException(
                "Dataset not found: "
                    + path
            );
        }


        List<CsvRow> rows =
            new ArrayList<>();


        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    path,
                    StandardCharsets.UTF_8
                )
        ) {

            String headerLine =
                reader.readLine();


            if (headerLine == null) {

                throw new IOException(
                    "Empty CSV: "
                        + path
                );
            }


            if (
                headerLine.startsWith(
                    "\uFEFF"
                )
            ) {

                headerLine =
                    headerLine.substring(1);
            }


            List<String> header =
                parseCsvLine(
                    headerLine,
                    1
                );


            if (
                !expectedHeader.equals(
                    header
                )
            ) {

                throw new IOException(
                    "Unexpected header in "
                        + path.getFileName()
                        + System.lineSeparator()
                        + "Expected: "
                        + String.join(
                            ",",
                            expectedHeader
                        )
                        + System.lineSeparator()
                        + "Actual:   "
                        + String.join(
                            ",",
                            header
                        )
                );
            }


            String line;
            int lineNumber = 1;


            while (
                (line = reader.readLine())
                    != null
            ) {

                lineNumber++;


                if (line.isBlank()) {
                    continue;
                }


                List<String> values =
                    parseCsvLine(
                        line,
                        lineNumber
                    );


                if (
                    values.size()
                        != header.size()
                ) {

                    throw new IOException(
                        "Column count mismatch in "
                            + path.getFileName()
                            + " at line "
                            + lineNumber
                            + ". Expected "
                            + header.size()
                            + ", found "
                            + values.size()
                            + "."
                    );
                }


                Map<String, String> valuesByName =
                    new HashMap<>();


                for (
                    int index = 0;
                    index < header.size();
                    index++
                ) {

                    valuesByName.put(
                        header.get(index),
                        values.get(index)
                    );
                }


                rows.add(
                    new CsvRow(
                        Map.copyOf(
                            valuesByName
                        )
                    )
                );
            }
        }


        return new CsvTable(
            List.copyOf(rows)
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
                "Unclosed quoted field at "
                    + "CSV line "
                    + lineNumber
            );
        }


        fields.add(
            current.toString()
        );


        return fields;
    }


    private static void writeOutput(
            Path outputPath,
            List<OutputRow> rows)
            throws IOException {

        Files.createDirectories(
            outputPath.getParent()
        );


        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    outputPath,
                    StandardCharsets.UTF_8
                )
        ) {

            writer.write(
                String.join(
                    ",",
                    OUTPUT_HEADER
                )
            );

            writer.newLine();


            for (OutputRow row : rows) {

                for (
                    int index = 0;
                    index
                        < row.values().size();
                    index++
                ) {

                    if (index > 0) {
                        writer.write(',');
                    }

                    writer.write(
                        escape(
                            row.values()
                                .get(index)
                        )
                    );
                }

                writer.newLine();
            }
        }
    }


    private static String required(
            CsvRow row,
            String column)
            throws IOException {

        String value =
            row.get(column);


        if (
            value == null
                || value.isBlank()
        ) {

            throw new IOException(
                "Missing required value: "
                    + column
            );
        }


        return value;
    }


    private static int parseInteger(
            String value,
            String field)
            throws IOException {

        try {

            return Integer.parseInt(
                value
            );

        } catch (
                NumberFormatException exception
        ) {

            throw new IOException(
                "Invalid integer for "
                    + field
                    + ": "
                    + value,
                exception
            );
        }
    }


    private static int parseNonNegativeInteger(
            String value,
            String field,
            ObservationKey key)
            throws IOException {

        int parsed =
            parseInteger(
                value,
                field
            );


        if (parsed < 0) {

            throw new IOException(
                "Negative "
                    + field
                    + " for "
                    + key
            );
        }


        return parsed;
    }


    private static String escape(
            String value) {

        if (value == null) {
            return "";
        }


        if (
            !value.contains(",")
                && !value.contains("\"")
                && !value.contains("\n")
                && !value.contains("\r")
        ) {

            return value;
        }


        return "\""
            + value.replace(
                "\"",
                "\"\""
            )
            + "\"";
    }


    private record ObservationKey(
        int releaseIndex,
        String filePath
    ) {
    }


    private record CsvTable(
        List<CsvRow> rows
    ) {
    }


    private record CsvRow(
        Map<String, String> values
    ) {

        String get(String column) {
            return values.get(column);
        }
    }


    private record OutputRow(
        List<String> values
    ) {
    }


    private static final class ReleaseSummary {

        private final int releaseIndex;
        private final String version;

        private int total;
        private int buggy;
        private int smelly;


        private ReleaseSummary(
                int releaseIndex,
                String version) {

            this.releaseIndex =
                releaseIndex;

            this.version =
                version;
        }


        private void add(
                boolean isBuggy,
                boolean hasSmells) {

            total++;

            if (isBuggy) {
                buggy++;
            }

            if (hasSmells) {
                smelly++;
            }
        }


        int releaseIndex() {
            return releaseIndex;
        }


        String version() {
            return version;
        }


        int total() {
            return total;
        }


        int buggy() {
            return buggy;
        }


        int smelly() {
            return smelly;
        }
    }
}