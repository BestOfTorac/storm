package it.uniroma2.isw2.storm.m3;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class M3SummaryGenerator {

    private static final int EXPECTED_TOTAL_ROWS = 34_560;

    private static final int EXPECTED_A_ROWS = 14_611;
    private static final int EXPECTED_BPLUS_ROWS = 5_338;
    private static final int EXPECTED_B_ROWS = 5_338;
    private static final int EXPECTED_C_ROWS = 9_273;

    private static final int EXPECTED_A_BUGGY = 1_243;
    private static final int EXPECTED_BPLUS_BUGGY = 610;
    private static final int EXPECTED_C_BUGGY = 633;

    private static final List<String> REQUIRED_COLUMNS =
            List.of(
                    "Dataset",
                    "RowIndex",
                    "Project",
                    "ReleaseIndex",
                    "Version",
                    "CommitId",
                    "FilePath",
                    "NSMELLS",
                    "ReferenceBUGGY",
                    "ActualAvailable",
                    "PredictedBUGGY",
                    "ProbabilityBUGGYYES"
            );

    private M3SummaryGenerator() {
        // Utility class.
    }

    public static void main(
            String[] args
    ) throws Exception {

        if (args.length != 0) {
            throw new IllegalArgumentException(
                    "M3SummaryGenerator does not require arguments."
            );
        }

        Path predictionsPath =
                Path.of(
                        "isw2",
                        "results",
                        "m3",
                        "what_if_predictions.csv"
                );

        Path bestClassifierPath =
                Path.of(
                        "isw2",
                        "results",
                        "m2",
                        "best_classifier.csv"
                );

        Path outputDirectory =
                Path.of(
                        "isw2",
                        "results",
                        "m3"
                );

        Files.createDirectories(
                outputDirectory
        );

        String bClassifier =
                readBClassifier(
                        bestClassifierPath
                );

        if (!"RandomForest".equals(
                bClassifier
        )) {
            throw new IllegalStateException(
                    "Expected M2 BClassifier RandomForest, found "
                            + bClassifier
            );
        }

        CsvData predictions =
                readCsv(
                        predictionsPath
                );

        validateHeader(
                predictions.header()
        );

        List<Prediction> rows =
                parsePredictions(
                        predictions
                );

        if (rows.size()
                != EXPECTED_TOTAL_ROWS) {

            throw new IllegalStateException(
                    "Expected "
                            + EXPECTED_TOTAL_ROWS
                            + " prediction rows, found "
                            + rows.size()
            );
        }

        Map<String, List<Prediction>> byDataset =
                splitByDataset(
                        rows
                );

        validateDatasetCounts(
                byDataset
        );

        DatasetStats a =
                summarize(
                        "A",
                        byDataset.get("A"),
                        true
                );

        DatasetStats bPlus =
                summarize(
                        "B+",
                        byDataset.get("B+"),
                        true
                );

        DatasetStats b =
                summarize(
                        "B",
                        byDataset.get("B"),
                        false
                );

        DatasetStats c =
                summarize(
                        "C",
                        byDataset.get("C"),
                        true
                );

        validateReferenceCounts(
                a,
                bPlus,
                b,
                c
        );

        validatePartitionPredictions(
                byDataset.get("A"),
                byDataset.get("B+"),
                byDataset.get("C")
        );

        TransitionStats transitions =
                analyzeCounterfactualPairs(
                        byDataset.get("B+"),
                        byDataset.get("B")
                );

        int estimatedPrevented =
                bPlus.referenceBuggyYes()
                        - b.predictedBuggyYes();

        double reductionAmongSmellyBuggy =
                percentage(
                        estimatedPrevented,
                        bPlus.referenceBuggyYes()
                );

        double reductionOverAllBuggy =
                percentage(
                        estimatedPrevented,
                        a.referenceBuggyYes()
                );

        int netPredictedReduction =
                bPlus.predictedBuggyYes()
                        - b.predictedBuggyYes();

        int referenceBuggyPredictedNoInB =
                countReferenceBuggyPredictedNo(
                        byDataset.get("B")
                );

        Path tablePath =
                outputDirectory.resolve(
                        "what_if_table.csv"
                );

        Path summaryPath =
                outputDirectory.resolve(
                        "what_if_summary.csv"
                );

        Path transitionsPath =
                outputDirectory.resolve(
                        "what_if_transitions.csv"
                );

        writeWhatIfTable(
                tablePath,
                List.of(
                        a,
                        bPlus,
                        b,
                        c
                )
        );

        writeSummary(
                summaryPath,
                bClassifier,
                a,
                bPlus,
                b,
                c,
                estimatedPrevented,
                reductionAmongSmellyBuggy,
                reductionOverAllBuggy,
                netPredictedReduction,
                transitions,
                referenceBuggyPredictedNoInB
        );

        writeTransitions(
                transitionsPath,
                transitions
        );

        System.out.println();
        System.out.println(
                "===== M3 SUMMARY GENERATOR ====="
        );

        System.out.println(
                "BClassifier              : "
                        + bClassifier
        );

        System.out.println();

        printDatasetStats(a);
        printDatasetStats(bPlus);
        printDatasetStats(b);
        printDatasetStats(c);

        System.out.println();
        System.out.println(
                "===== OFFICIAL WHAT-IF ====="
        );

        System.out.println(
                "Actual BUGGY in B+        : "
                        + bPlus.referenceBuggyYes()
        );

        System.out.println(
                "Estimated BUGGY in B      : "
                        + b.predictedBuggyYes()
        );

        System.out.println(
                "Estimated prevented       : "
                        + estimatedPrevented
        );

        System.out.printf(
                Locale.ROOT,
                "Reduction among B+ BUGGY  : %.4f%%%n",
                reductionAmongSmellyBuggy
        );

        System.out.printf(
                Locale.ROOT,
                "Reduction over all BUGGY  : %.4f%%%n",
                reductionOverAllBuggy
        );

        System.out.println();
        System.out.println(
                "===== PAIRWISE B+ -> B ====="
        );

        System.out.println(
                "YES -> YES               : "
                        + transitions.yesToYes()
        );

        System.out.println(
                "YES -> NO                : "
                        + transitions.yesToNo()
        );

        System.out.println(
                "NO  -> YES               : "
                        + transitions.noToYes()
        );

        System.out.println(
                "NO  -> NO                : "
                        + transitions.noToNo()
        );

        System.out.println(
                "Net predicted reduction  : "
                        + netPredictedReduction
        );

        System.out.println(
                "Reference BUGGY -> NO in B: "
                        + referenceBuggyPredictedNoInB
        );

        System.out.println();
        System.out.println(
                "what_if_table.csv        : "
                        + tablePath.toAbsolutePath()
        );

        System.out.println(
                "what_if_summary.csv      : "
                        + summaryPath.toAbsolutePath()
        );

        System.out.println(
                "what_if_transitions.csv  : "
                        + transitionsPath.toAbsolutePath()
        );

        System.out.println();
        System.out.println(
                "RESULT: M3 SUMMARY OK"
        );
    }

    private static Map<String, List<Prediction>> splitByDataset(
            List<Prediction> rows
    ) {

        Map<String, List<Prediction>> result =
                new LinkedHashMap<>();

        result.put("A", new ArrayList<>());
        result.put("B+", new ArrayList<>());
        result.put("B", new ArrayList<>());
        result.put("C", new ArrayList<>());

        for (Prediction row : rows) {

            List<Prediction> target =
                    result.get(
                            row.dataset()
                    );

            if (target == null) {
                throw new IllegalStateException(
                        "Unexpected dataset: "
                                + row.dataset()
                );
            }

            target.add(row);
        }

        return result;
    }

    private static void validateDatasetCounts(
            Map<String, List<Prediction>> byDataset
    ) {

        requireCount(
                "A",
                byDataset.get("A"),
                EXPECTED_A_ROWS
        );

        requireCount(
                "B+",
                byDataset.get("B+"),
                EXPECTED_BPLUS_ROWS
        );

        requireCount(
                "B",
                byDataset.get("B"),
                EXPECTED_B_ROWS
        );

        requireCount(
                "C",
                byDataset.get("C"),
                EXPECTED_C_ROWS
        );
    }

    private static void requireCount(
            String name,
            List<Prediction> rows,
            int expected
    ) {

        if (rows == null
                || rows.size() != expected) {

            throw new IllegalStateException(
                    name
                            + " expected "
                            + expected
                            + " rows."
            );
        }
    }

    private static DatasetStats summarize(
            String name,
            List<Prediction> rows,
            boolean actualAvailable
    ) {

        int referenceBuggyYes = 0;
        int predictedBuggyYes = 0;
        double probabilitySum = 0.0;

        for (Prediction row : rows) {

            boolean rowActual =
                    "Yes".equals(
                            row.actualAvailable()
                    );

            if (rowActual != actualAvailable) {
                throw new IllegalStateException(
                        name
                                + " has inconsistent ActualAvailable."
                );
            }

            if ("YES".equals(
                    row.referenceBuggy()
            )) {
                referenceBuggyYes++;
            }

            if ("YES".equals(
                    row.predictedBuggy()
            )) {
                predictedBuggyYes++;
            }

            probabilitySum +=
                    row.probabilityYes();
        }

        return new DatasetStats(
                name,
                rows.size(),
                actualAvailable,
                referenceBuggyYes,
                predictedBuggyYes,
                probabilitySum / rows.size()
        );
    }

    private static void validateReferenceCounts(
            DatasetStats a,
            DatasetStats bPlus,
            DatasetStats b,
            DatasetStats c
    ) {

        if (a.referenceBuggyYes()
                != EXPECTED_A_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected A BUGGY count."
            );
        }

        if (bPlus.referenceBuggyYes()
                != EXPECTED_BPLUS_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected B+ BUGGY count."
            );
        }

        if (b.referenceBuggyYes()
                != EXPECTED_BPLUS_BUGGY) {

            throw new IllegalStateException(
                    "B did not preserve B+ reference labels."
            );
        }

        if (c.referenceBuggyYes()
                != EXPECTED_C_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected C BUGGY count."
            );
        }

        if (bPlus.referenceBuggyYes()
                + c.referenceBuggyYes()
                != a.referenceBuggyYes()) {

            throw new IllegalStateException(
                    "Reference BUGGY partition failed."
            );
        }

        if (bPlus.predictedBuggyYes()
                + c.predictedBuggyYes()
                != a.predictedBuggyYes()) {

            throw new IllegalStateException(
                    "Predicted A != predicted B+ + predicted C."
            );
        }
    }

    private static void validatePartitionPredictions(
            List<Prediction> aRows,
            List<Prediction> bPlusRows,
            List<Prediction> cRows
    ) {

        Map<String, Prediction> aByKey =
                indexByKey(
                        aRows,
                        "A"
                );

        if (aByKey.size()
                != EXPECTED_A_ROWS) {

            throw new IllegalStateException(
                    "A does not contain unique observation keys."
            );
        }

        int matched = 0;

        for (Prediction row :
                concat(
                        bPlusRows,
                        cRows
                )) {

            Prediction original =
                    aByKey.get(
                            row.key()
                    );

            if (original == null) {
                throw new IllegalStateException(
                        "B+/C observation not found in A: "
                                + row.key()
                );
            }

            if (!original.referenceBuggy()
                    .equals(
                            row.referenceBuggy()
                    )) {

                throw new IllegalStateException(
                        "Reference BUGGY differs from A."
                );
            }

            if (!original.predictedBuggy()
                    .equals(
                            row.predictedBuggy()
                    )) {

                throw new IllegalStateException(
                        "Prediction on unchanged A observation differs."
                );
            }

            if (Double.compare(
                    original.probabilityYes(),
                    row.probabilityYes()
            ) != 0) {

                throw new IllegalStateException(
                        "Probability on unchanged A observation differs."
                );
            }

            matched++;
        }

        if (matched != EXPECTED_A_ROWS) {
            throw new IllegalStateException(
                    "A partition matching failed."
            );
        }
    }

    private static TransitionStats analyzeCounterfactualPairs(
            List<Prediction> bPlus,
            List<Prediction> b
    ) {

        if (bPlus.size()
                != b.size()) {

            throw new IllegalStateException(
                    "B+ and B size mismatch."
            );
        }

        int yesToYes = 0;
        int yesToNo = 0;
        int noToYes = 0;
        int noToNo = 0;

        for (int i = 0;
             i < bPlus.size();
             i++) {

            Prediction real =
                    bPlus.get(i);

            Prediction counterfactual =
                    b.get(i);

            if (real.rowIndex()
                    != counterfactual.rowIndex()) {

                throw new IllegalStateException(
                        "B+/B row index mismatch at "
                                + (i + 1)
                );
            }

            if (!real.key()
                    .equals(
                            counterfactual.key()
                    )) {

                throw new IllegalStateException(
                        "B+/B observation mismatch at row "
                                + (i + 1)
                );
            }

            if (!real.referenceBuggy()
                    .equals(
                            counterfactual.referenceBuggy()
                    )) {

                throw new IllegalStateException(
                        "B reference label differs from B+."
                );
            }

            if (!(real.nSmells() > 0.0)) {
                throw new IllegalStateException(
                        "B+ NSMELLS must be > 0."
                );
            }

            if (Double.compare(
                    counterfactual.nSmells(),
                    0.0
            ) != 0) {

                throw new IllegalStateException(
                        "B NSMELLS must be 0."
                );
            }

            String from =
                    real.predictedBuggy();

            String to =
                    counterfactual.predictedBuggy();

            if ("YES".equals(from)
                    && "YES".equals(to)) {

                yesToYes++;

            } else if ("YES".equals(from)
                    && "NO".equals(to)) {

                yesToNo++;

            } else if ("NO".equals(from)
                    && "YES".equals(to)) {

                noToYes++;

            } else if ("NO".equals(from)
                    && "NO".equals(to)) {

                noToNo++;

            } else {

                throw new IllegalStateException(
                        "Invalid transition: "
                                + from
                                + " -> "
                                + to
                );
            }
        }

        if (yesToYes
                + yesToNo
                + noToYes
                + noToNo
                != EXPECTED_BPLUS_ROWS) {

            throw new IllegalStateException(
                    "Transition counts do not sum to B+."
            );
        }

        return new TransitionStats(
                yesToYes,
                yesToNo,
                noToYes,
                noToNo
        );
    }

    private static int countReferenceBuggyPredictedNo(
            List<Prediction> rows
    ) {

        int count = 0;

        for (Prediction row : rows) {

            if ("YES".equals(
                    row.referenceBuggy()
            )
                    && "NO".equals(
                    row.predictedBuggy()
            )) {

                count++;
            }
        }

        return count;
    }

    private static Map<String, Prediction> indexByKey(
            List<Prediction> rows,
            String name
    ) {

        Map<String, Prediction> result =
                new HashMap<>();

        for (Prediction row : rows) {

            Prediction previous =
                    result.put(
                            row.key(),
                            row
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        name
                                + " duplicate observation key: "
                                + row.key()
                );
            }
        }

        return result;
    }

    private static List<Prediction> concat(
            List<Prediction> first,
            List<Prediction> second
    ) {

        List<Prediction> result =
                new ArrayList<>(
                        first.size()
                                + second.size()
                );

        result.addAll(first);
        result.addAll(second);

        return result;
    }

    private static void writeWhatIfTable(
            Path path,
            List<DatasetStats> stats
    ) throws Exception {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Dataset,Rows,ActualBUGGY,EstimatedBUGGY,"
                            + "MeanProbabilityBUGGYYES,ActualAvailable"
            );

            writer.newLine();

            for (DatasetStats stat : stats) {

                String actual =
                        stat.actualAvailable()
                                ? Integer.toString(
                                stat.referenceBuggyYes()
                        )
                                : "";

                writer.write(
                        stat.dataset()
                                + ","
                                + stat.rows()
                                + ","
                                + actual
                                + ","
                                + stat.predictedBuggyYes()
                                + ","
                                + format(
                                stat.meanProbabilityYes()
                        )
                                + ","
                                + (
                                stat.actualAvailable()
                                        ? "Yes"
                                        : "No"
                        )
                );

                writer.newLine();
            }
        }
    }

    private static void writeSummary(
            Path path,
            String classifier,
            DatasetStats a,
            DatasetStats bPlus,
            DatasetStats b,
            DatasetStats c,
            int estimatedPrevented,
            double reductionAmongSmellyBuggy,
            double reductionOverAllBuggy,
            int netPredictedReduction,
            TransitionStats transitions,
            int referenceBuggyPredictedNoInB
    ) throws Exception {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Dataset,BClassifier,"
                            + "ActualBuggyA,EstimatedBuggyA,"
                            + "ActualBuggyBPlus,EstimatedBuggyBPlus,"
                            + "EstimatedBuggyB,"
                            + "ActualBuggyC,EstimatedBuggyC,"
                            + "EstimatedPreventedBuggy,"
                            + "ReductionAmongSmellyBuggyPct,"
                            + "ReductionOverAllBuggyPct,"
                            + "NetPredictedBuggyReduction,"
                            + "YesToNo,NoToYes,"
                            + "ReferenceBuggyPredictedNoInB"
            );

            writer.newLine();

            writer.write(
                    "STORM,"
                            + classifier
                            + ","
                            + a.referenceBuggyYes()
                            + ","
                            + a.predictedBuggyYes()
                            + ","
                            + bPlus.referenceBuggyYes()
                            + ","
                            + bPlus.predictedBuggyYes()
                            + ","
                            + b.predictedBuggyYes()
                            + ","
                            + c.referenceBuggyYes()
                            + ","
                            + c.predictedBuggyYes()
                            + ","
                            + estimatedPrevented
                            + ","
                            + format(
                            reductionAmongSmellyBuggy
                    )
                            + ","
                            + format(
                            reductionOverAllBuggy
                    )
                            + ","
                            + netPredictedReduction
                            + ","
                            + transitions.yesToNo()
                            + ","
                            + transitions.noToYes()
                            + ","
                            + referenceBuggyPredictedNoInB
            );

            writer.newLine();
        }
    }

    private static void writeTransitions(
            Path path,
            TransitionStats transitions
    ) throws Exception {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "FromPredictedBUGGY,ToPredictedBUGGY,Count"
            );

            writer.newLine();

            writer.write(
                    "YES,YES,"
                            + transitions.yesToYes()
            );

            writer.newLine();

            writer.write(
                    "YES,NO,"
                            + transitions.yesToNo()
            );

            writer.newLine();

            writer.write(
                    "NO,YES,"
                            + transitions.noToYes()
            );

            writer.newLine();

            writer.write(
                    "NO,NO,"
                            + transitions.noToNo()
            );

            writer.newLine();
        }
    }

    private static List<Prediction> parsePredictions(
            CsvData csv
    ) {

        Map<String, Integer> indexes =
                new HashMap<>();

        for (int i = 0;
             i < csv.header().size();
             i++) {

            indexes.put(
                    csv.header().get(i),
                    i
            );
        }

        List<Prediction> result =
                new ArrayList<>();

        for (List<String> row :
                csv.rows()) {

            if (row.size()
                    != csv.header().size()) {

                throw new IllegalStateException(
                        "Malformed prediction CSV row."
                );
            }

            String predicted =
                    value(
                            row,
                            indexes,
                            "PredictedBUGGY"
                    );

            String reference =
                    value(
                            row,
                            indexes,
                            "ReferenceBUGGY"
                    );

            if (!List.of(
                    "YES",
                    "NO"
            ).contains(predicted)) {

                throw new IllegalStateException(
                        "Invalid PredictedBUGGY: "
                                + predicted
                );
            }

            if (!List.of(
                    "YES",
                    "NO"
            ).contains(reference)) {

                throw new IllegalStateException(
                        "Invalid ReferenceBUGGY: "
                                + reference
                );
            }

            double probability =
                    Double.parseDouble(
                            value(
                                    row,
                                    indexes,
                                    "ProbabilityBUGGYYES"
                            )
                    );

            if (!Double.isFinite(probability)
                    || probability < 0.0
                    || probability > 1.0) {

                throw new IllegalStateException(
                        "Invalid probability: "
                                + probability
                );
            }

            result.add(
                    new Prediction(
                            value(
                                    row,
                                    indexes,
                                    "Dataset"
                            ),
                            Integer.parseInt(
                                    value(
                                            row,
                                            indexes,
                                            "RowIndex"
                                    )
                            ),
                            value(
                                    row,
                                    indexes,
                                    "Project"
                            ),
                            value(
                                    row,
                                    indexes,
                                    "ReleaseIndex"
                            ),
                            value(
                                    row,
                                    indexes,
                                    "Version"
                            ),
                            value(
                                    row,
                                    indexes,
                                    "CommitId"
                            ),
                            value(
                                    row,
                                    indexes,
                                    "FilePath"
                            ),
                            Double.parseDouble(
                                    value(
                                            row,
                                            indexes,
                                            "NSMELLS"
                                    )
                            ),
                            reference,
                            value(
                                    row,
                                    indexes,
                                    "ActualAvailable"
                            ),
                            predicted,
                            probability
                    )
            );
        }

        return result;
    }

    private static String value(
            List<String> row,
            Map<String, Integer> indexes,
            String name
    ) {

        Integer index =
                indexes.get(name);

        if (index == null) {
            throw new IllegalStateException(
                    "Missing column: "
                            + name
            );
        }

        return row.get(index);
    }

    private static void validateHeader(
            List<String> header
    ) {

        if (!header.equals(
                REQUIRED_COLUMNS
        )) {

            throw new IllegalStateException(
                    "Unexpected prediction CSV header."
                            + System.lineSeparator()
                            + "Expected: "
                            + REQUIRED_COLUMNS
                            + System.lineSeparator()
                            + "Actual  : "
                            + header
            );
        }
    }

    private static String readBClassifier(
            Path path
    ) throws Exception {

        CsvData csv =
                readCsv(
                        path
                );

        if (csv.rows().size() != 1) {
            throw new IllegalStateException(
                    "Expected one best-classifier row."
            );
        }

        int datasetIndex =
                csv.header()
                        .indexOf(
                                "Dataset"
                        );

        int classifierIndex =
                csv.header()
                        .indexOf(
                                "BClassifier"
                        );

        if (datasetIndex < 0
                || classifierIndex < 0) {

            throw new IllegalStateException(
                    "Malformed best_classifier.csv."
            );
        }

        List<String> row =
                csv.rows().get(0);

        if (!"STORM".equals(
                row.get(datasetIndex)
        )) {

            throw new IllegalStateException(
                    "best_classifier.csv is not for STORM."
            );
        }

        return row.get(
                classifierIndex
        );
    }

    private static CsvData readCsv(
            Path path
    ) throws Exception {

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "File not found: "
                            + path.toAbsolutePath()
            );
        }

        List<String> lines =
                Files.readAllLines(
                        path,
                        StandardCharsets.UTF_8
                );

        if (lines.isEmpty()) {
            throw new IllegalStateException(
                    "CSV is empty: "
                            + path
            );
        }

        List<String> header =
                parseCsvLine(
                        removeBom(
                                lines.get(0)
                        )
                );

        List<List<String>> rows =
                new ArrayList<>();

        for (int i = 1;
             i < lines.size();
             i++) {

            if (lines.get(i).isBlank()) {
                continue;
            }

            rows.add(
                    parseCsvLine(
                            lines.get(i)
                    )
            );
        }

        return new CsvData(
                header,
                rows
        );
    }

    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> fields =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted = false;

        for (int i = 0;
             i < line.length();
             i++) {

            char ch =
                    line.charAt(i);

            if (ch == '"') {

                if (quoted
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {

                    quoted =
                            !quoted;
                }

            } else if (ch == ','
                    && !quoted) {

                fields.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(ch);
            }
        }

        if (quoted) {
            throw new IllegalStateException(
                    "Unclosed CSV quote."
            );
        }

        fields.add(
                current.toString()
        );

        return fields;
    }

    private static String removeBom(
            String value
    ) {

        if (!value.isEmpty()
                && value.charAt(0) == '\uFEFF') {

            return value.substring(1);
        }

        return value;
    }

    private static double percentage(
            int numerator,
            int denominator
    ) {

        if (denominator == 0) {
            throw new IllegalArgumentException(
                    "Zero denominator."
            );
        }

        return 100.0
                * numerator
                / denominator;
    }

    private static String format(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.10f",
                value
        );
    }

    private static void printDatasetStats(
            DatasetStats stats
    ) {

        String actual =
                stats.actualAvailable()
                        ? Integer.toString(
                        stats.referenceBuggyYes()
                )
                        : "N/A";

        System.out.printf(
                Locale.ROOT,
                "%-3s rows=%5d | actual=%4s | estimated=%4d | mean P(YES)=%.6f%n",
                stats.dataset(),
                stats.rows(),
                actual,
                stats.predictedBuggyYes(),
                stats.meanProbabilityYes()
        );
    }

    private record Prediction(
            String dataset,
            int rowIndex,
            String project,
            String releaseIndex,
            String version,
            String commitId,
            String filePath,
            double nSmells,
            String referenceBuggy,
            String actualAvailable,
            String predictedBuggy,
            double probabilityYes
    ) {

        String key() {
            return project
                    + "\u001F"
                    + releaseIndex
                    + "\u001F"
                    + version
                    + "\u001F"
                    + commitId
                    + "\u001F"
                    + filePath;
        }
    }

    private record DatasetStats(
            String dataset,
            int rows,
            boolean actualAvailable,
            int referenceBuggyYes,
            int predictedBuggyYes,
            double meanProbabilityYes
    ) {
    }

    private record TransitionStats(
            int yesToYes,
            int yesToNo,
            int noToYes,
            int noToNo
    ) {
    }

    private record CsvData(
            List<String> header,
            List<List<String>> rows
    ) {
    }
}