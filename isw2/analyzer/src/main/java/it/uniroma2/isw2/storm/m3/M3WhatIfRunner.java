package it.uniroma2.isw2.storm.m3;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.uniroma2.isw2.storm.m2.M2ClassifierFactory;
import it.uniroma2.isw2.storm.m2.M2ClassifierFactory.ClassifierKind;
import it.uniroma2.isw2.storm.m2.M2DatasetLoader;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;

public final class M3WhatIfRunner {

    private static final String CLASS_ATTRIBUTE =
            "BUGGY";

    private static final String SMELL_ATTRIBUTE =
            "NSMELLS";

    private static final int EXPECTED_A_ROWS =
            14_611;

    private static final int EXPECTED_BPLUS_ROWS =
            5_338;

    private static final int EXPECTED_B_ROWS =
            5_338;

    private static final int EXPECTED_C_ROWS =
            9_273;

    private static final int EXPECTED_A_BUGGY =
            1_243;

    private static final int EXPECTED_BPLUS_BUGGY =
            610;

    private static final int EXPECTED_C_BUGGY =
            633;

    private M3WhatIfRunner() {
        // Utility class.
    }

    public static void main(
            String[] args
    ) throws Exception {

        if (args.length != 0) {
            throw new IllegalArgumentException(
                    "M3WhatIfRunner does not require arguments."
            );
        }

        Path aPath =
                Path.of(
                        "isw2",
                        "datasets",
                        "storm_m1_dataset_sonarcloud.csv"
                );

        Path bestClassifierPath =
                Path.of(
                        "isw2",
                        "results",
                        "m2",
                        "best_classifier.csv"
                );

        Path bPlusPath =
                Path.of(
                        "isw2",
                        "datasets",
                        "m3",
                        "storm_m3_bplus.csv"
                );

        Path bPath =
                Path.of(
                        "isw2",
                        "datasets",
                        "m3",
                        "storm_m3_b.csv"
                );

        Path cPath =
                Path.of(
                        "isw2",
                        "datasets",
                        "m3",
                        "storm_m3_c.csv"
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

        Path predictionsPath =
                outputDirectory.resolve(
                        "what_if_predictions.csv"
                );

        /*
         * Load A through the already validated M2 loader.
         */
        M2DatasetLoader.DatasetBundle aBundle =
                M2DatasetLoader.load(
                        aPath
                );

        Instances aRaw =
                new Instances(
                        aBundle.raw()
                );

        Instances aModeling =
                new Instances(
                        aBundle.modeling()
                );

        validateBClassifier(
                bestClassifierPath
        );

        /*
         * Load the three persisted M3 datasets.
         */
        Instances bPlusRaw =
                loadCsv(
                        bPlusPath
                );

        Instances bRaw =
                loadCsv(
                        bPath
                );

        Instances cRaw =
                loadCsv(
                        cPath
                );

        validateRawDataset(
                "B+",
                aRaw,
                bPlusRaw,
                EXPECTED_BPLUS_ROWS,
                SmellMode.POSITIVE
        );

        validateRawDataset(
                "B",
                aRaw,
                bRaw,
                EXPECTED_B_ROWS,
                SmellMode.ZERO
        );

        validateRawDataset(
                "C",
                aRaw,
                cRaw,
                EXPECTED_C_ROWS,
                SmellMode.ZERO
        );

        validateCounterfactualPair(
                bPlusRaw,
                bRaw
        );

        validateBuggyCounts(
                aRaw,
                bPlusRaw,
                bRaw,
                cRaw
        );

        /*
         * Important:
         *
         * B+, B and C are converted using A's modeling header.
         * This guarantees exactly the same:
         *
         * - predictor order
         * - attribute types
         * - BUGGY nominal domain
         * - class index
         *
         * used to train BClassifierA.
         */
        Instances bPlusModeling =
                toModelingWithAHeader(
                        bPlusRaw,
                        aModeling,
                        "STORM_M3_BPLUS_MODELING"
                );

        Instances bModeling =
                toModelingWithAHeader(
                        bRaw,
                        aModeling,
                        "STORM_M3_B_MODELING"
                );

        Instances cModeling =
                toModelingWithAHeader(
                        cRaw,
                        aModeling,
                        "STORM_M3_C_MODELING"
                );

        validateModelingHeaders(
                aModeling,
                bPlusModeling,
                "B+"
        );

        validateModelingHeaders(
                aModeling,
                bModeling,
                "B"
        );

        validateModelingHeaders(
                aModeling,
                cModeling,
                "C"
        );

        /*
         * M3 requirement:
         *
         * Train the BClassifier selected in M2 on the entire
         * real Dataset A.
         *
         * No feature selection.
         * No SMOTE.
         */
        Classifier classifier =
                M2ClassifierFactory.create(
                        ClassifierKind.RANDOM_FOREST
                );

        classifier.buildClassifier(
                aModeling
        );

        int yesIndex =
                aModeling.classAttribute()
                        .indexOfValue(
                                "YES"
                        );

        if (yesIndex < 0) {
            throw new IllegalStateException(
                    "BUGGY=YES not found in A class domain."
            );
        }

        List<PredictionRow> predictionRows =
                new ArrayList<>();

        DatasetSummary aSummary =
                predictDataset(
                        "A",
                        aRaw,
                        aModeling,
                        classifier,
                        yesIndex,
                        true,
                        predictionRows
                );

        DatasetSummary bPlusSummary =
                predictDataset(
                        "B+",
                        bPlusRaw,
                        bPlusModeling,
                        classifier,
                        yesIndex,
                        true,
                        predictionRows
                );

        DatasetSummary bSummary =
                predictDataset(
                        "B",
                        bRaw,
                        bModeling,
                        classifier,
                        yesIndex,
                        false,
                        predictionRows
                );

        DatasetSummary cSummary =
                predictDataset(
                        "C",
                        cRaw,
                        cModeling,
                        classifier,
                        yesIndex,
                        true,
                        predictionRows
                );

        validatePredictionCounts(
                predictionRows
        );

        writePredictions(
                predictionsPath,
                predictionRows
        );

        System.out.println();
        System.out.println(
                "===== M3 WHAT-IF RUNNER ====="
        );

        System.out.println(
                "BClassifierA            : RandomForest"
        );

        System.out.println(
                "Training dataset         : A"
        );

        System.out.println(
                "Training rows            : "
                        + aModeling.numInstances()
        );

        System.out.println(
                "Predictors               : "
                        + (
                        aModeling.numAttributes()
                                - 1
                )
        );

        System.out.println(
                "Feature selection        : No"
        );

        System.out.println(
                "Balancing                : No"
        );

        System.out.println();

        printSummary(
                aSummary
        );

        printSummary(
                bPlusSummary
        );

        printSummary(
                bSummary
        );

        printSummary(
                cSummary
        );

        System.out.println();

        System.out.println(
                "Prediction rows          : "
                        + predictionRows.size()
        );

        System.out.println(
                "Predictions CSV          : "
                        + predictionsPath
                        .toAbsolutePath()
        );

        System.out.println();

        System.out.println(
                "Classifier options       : "
                        + M2ClassifierFactory.options(
                        classifier
                )
        );

        System.out.println();

        System.out.println(
                "RESULT: M3 WHAT-IF PREDICTIONS OK"
        );
    }

    private static DatasetSummary predictDataset(
            String datasetName,
            Instances raw,
            Instances modeling,
            Classifier classifier,
            int yesIndex,
            boolean actualAvailable,
            List<PredictionRow> output
    ) throws Exception {

        if (raw.numInstances()
                != modeling.numInstances()) {

            throw new IllegalStateException(
                    datasetName
                            + " raw/modeling row mismatch."
            );
        }

        int referenceBuggyYes = 0;
        int predictedBuggyYes = 0;

        double probabilitySum = 0.0;

        for (int rowIndex = 0;
             rowIndex < raw.numInstances();
             rowIndex++) {

            Instance rawInstance =
                    raw.instance(
                            rowIndex
                    );

            Instance source =
                    modeling.instance(
                            rowIndex
                    );

            String referenceBuggy =
                    source.stringValue(
                            modeling.classAttribute()
                    );

            if ("YES".equals(
                    referenceBuggy
            )) {
                referenceBuggyYes++;
            }

            /*
             * Never expose BUGGY to the classifier at prediction time.
             *
             * Even though WEKA classifiers normally ignore the class
             * attribute during classification, explicitly clearing it
             * makes the prediction contract unambiguous.
             */
            Instance scoringInstance =
                    (Instance) source.copy();

            scoringInstance.setDataset(
                    modeling
            );

            scoringInstance.setMissing(
                    modeling.classIndex()
            );

            double[] distribution =
                    classifier.distributionForInstance(
                            scoringInstance
                    );

            if (distribution.length
                    != modeling.classAttribute()
                    .numValues()) {

                throw new IllegalStateException(
                        datasetName
                                + " returned invalid probability distribution."
                );
            }

            double probabilityYes =
                    distribution[
                            yesIndex
                            ];

            if (!Double.isFinite(
                    probabilityYes
            )
                    || probabilityYes < 0.0
                    || probabilityYes > 1.0) {

                throw new IllegalStateException(
                        datasetName
                                + " produced invalid P(BUGGY=YES): "
                                + probabilityYes
                );
            }

            int predictedIndex =
                    Utils.maxIndex(
                            distribution
                    );

            String predictedBuggy =
                    modeling.classAttribute()
                    .value(
                            predictedIndex
                    );

            if (!"YES".equals(
                    predictedBuggy
            )
                    && !"NO".equals(
                    predictedBuggy
            )) {

                throw new IllegalStateException(
                        "Unexpected predicted class: "
                                + predictedBuggy
                );
            }

            if ("YES".equals(
                    predictedBuggy
            )) {
                predictedBuggyYes++;
            }

            probabilitySum +=
                    probabilityYes;

            output.add(
                    new PredictionRow(
                            datasetName,
                            rowIndex + 1,
                            cell(
                                    raw,
                                    rawInstance,
                                    "Project"
                            ),
                            cell(
                                    raw,
                                    rawInstance,
                                    "ReleaseIndex"
                            ),
                            cell(
                                    raw,
                                    rawInstance,
                                    "Version"
                            ),
                            cell(
                                    raw,
                                    rawInstance,
                                    "CommitId"
                            ),
                            cell(
                                    raw,
                                    rawInstance,
                                    "FilePath"
                            ),
                            rawInstance.value(
                                    requireAttribute(
                                            raw,
                                            SMELL_ATTRIBUTE
                                    )
                            ),
                            referenceBuggy,
                            actualAvailable,
                            predictedBuggy,
                            probabilityYes
                    )
            );
        }

        double meanProbabilityYes =
                probabilitySum
                        / modeling.numInstances();

        return new DatasetSummary(
                datasetName,
                modeling.numInstances(),
                actualAvailable,
                referenceBuggyYes,
                predictedBuggyYes,
                meanProbabilityYes
        );
    }

    private static Instances toModelingWithAHeader(
            Instances raw,
            Instances aModeling,
            String relationName
    ) {

        Instances modeling =
                new Instances(
                        aModeling,
                        0
                );

        modeling.setRelationName(
                relationName
        );

        modeling.setClassIndex(
                aModeling.classIndex()
        );

        for (Instance rawInstance : raw) {

            DenseInstance transformed =
                    new DenseInstance(
                            modeling.numAttributes()
                    );

            transformed.setDataset(
                    modeling
            );

            for (int attributeIndex = 0;
                 attributeIndex < modeling.numAttributes() - 1;
                 attributeIndex++) {

                Attribute modelAttribute =
                        modeling.attribute(
                                attributeIndex
                        );

                Attribute rawAttribute =
                        requireAttribute(
                                raw,
                                modelAttribute.name()
                        );

                if (!rawAttribute.isNumeric()) {
                    throw new IllegalStateException(
                            "Expected numeric predictor in "
                                    + raw.relationName()
                                    + ": "
                                    + rawAttribute.name()
                    );
                }

                if (rawInstance.isMissing(
                        rawAttribute
                )) {
                    throw new IllegalStateException(
                            "Missing predictor "
                                    + rawAttribute.name()
                    );
                }

                transformed.setValue(
                        attributeIndex,
                        rawInstance.value(
                                rawAttribute
                        )
                );
            }

            Attribute rawClass =
                    requireAttribute(
                            raw,
                            CLASS_ATTRIBUTE
                    );

            if (rawInstance.isMissing(
                    rawClass
            )) {
                throw new IllegalStateException(
                        "Missing BUGGY value."
                );
            }

            String classLabel =
                    rawInstance.stringValue(
                            rawClass
                    );

            int classIndex =
                    modeling.classAttribute()
                    .indexOfValue(
                            classLabel
                    );

            if (classIndex < 0) {
                throw new IllegalStateException(
                        "BUGGY value not present in A header: "
                                + classLabel
                );
            }

            transformed.setValue(
                    modeling.classIndex(),
                    classIndex
            );

            modeling.add(
                    transformed
            );
        }

        return modeling;
    }

    private static void validateModelingHeaders(
            Instances a,
            Instances candidate,
            String name
    ) {

        String message =
                a.equalHeadersMsg(
                        candidate
                );

        if (message != null) {
            throw new IllegalStateException(
                    name
                            + " modeling header differs from A: "
                            + message
            );
        }
    }

    private static void validateBClassifier(
            Path path
    ) throws Exception {

        if (!Files.isRegularFile(
                path
        )) {
            throw new IllegalArgumentException(
                    "Missing M2 best_classifier.csv: "
                            + path
            );
        }

        List<String> lines =
                Files.readAllLines(
                        path,
                        StandardCharsets.UTF_8
                );

        if (lines.size() != 2) {
            throw new IllegalStateException(
                    "Expected exactly one BClassifier row."
            );
        }

        String[] header =
                lines.get(0)
                        .split(
                                ",",
                                -1
                        );

        String[] values =
                lines.get(1)
                        .split(
                                ",",
                                -1
                        );

        if (header.length
                != values.length) {

            throw new IllegalStateException(
                    "Malformed best_classifier.csv."
            );
        }

        Map<String, String> row =
                new HashMap<>();

        for (int i = 0;
             i < header.length;
             i++) {

            String key =
                    cleanCsvToken(
                            header[i]
                    );

            String value =
                    cleanCsvToken(
                            values[i]
                    );

            row.put(
                    key,
                    value
            );
        }

        String project =
                row.get(
                        "Dataset"
                );

        String classifier =
                row.get(
                        "BClassifier"
                );

        if (!"STORM".equals(
                project
        )) {
            throw new IllegalStateException(
                    "Expected STORM in best_classifier.csv."
            );
        }

        if (!"RandomForest".equals(
                classifier
        )) {
            throw new IllegalStateException(
                    "M3 requires the M2 BClassifier; found: "
                            + classifier
            );
        }
    }

    private static String cleanCsvToken(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value
                        .replace(
                                "\uFEFF",
                                ""
                        )
                        .trim();

        if (cleaned.length() >= 2
                && cleaned.startsWith("\"")
                && cleaned.endsWith("\"")) {

            cleaned =
                    cleaned.substring(
                            1,
                            cleaned.length() - 1
                    )
                            .replace(
                                    "\"\"",
                                    "\""
                            );
        }

        return cleaned;
    }
    private static void validateRawDataset(
            String name,
            Instances a,
            Instances candidate,
            int expectedRows,
            SmellMode smellMode
    ) {

        if (candidate.numInstances()
                != expectedRows) {

            throw new IllegalStateException(
                    name
                            + " expected "
                            + expectedRows
                            + " rows, found "
                            + candidate.numInstances()
            );
        }

        if (candidate.numAttributes()
                != a.numAttributes()) {

            throw new IllegalStateException(
                    name
                            + " attribute count differs from A."
            );
        }

        for (int i = 0;
             i < a.numAttributes();
             i++) {

            String expected =
                    a.attribute(i)
                            .name();

            String actual =
                    candidate.attribute(i)
                            .name();

            if (!expected.equals(
                    actual
            )) {
                throw new IllegalStateException(
                        name
                                + " schema mismatch at attribute "
                                + (i + 1)
                );
            }
        }

        Attribute smells =
                requireAttribute(
                        candidate,
                        SMELL_ATTRIBUTE
                );

        for (int row = 0;
             row < candidate.numInstances();
             row++) {

            double value =
                    candidate.instance(row)
                            .value(
                                    smells
                            );

            if (smellMode
                    == SmellMode.POSITIVE) {

                if (!(value > 0.0)) {
                    throw new IllegalStateException(
                            name
                                    + " contains NSMELLS <= 0."
                    );
                }

            } else {

                if (Double.compare(
                        value,
                        0.0
                ) != 0) {

                    throw new IllegalStateException(
                            name
                                    + " contains NSMELLS != 0."
                    );
                }
            }
        }
    }

    private static void validateCounterfactualPair(
            Instances bPlus,
            Instances b
    ) {

        if (bPlus.numInstances()
                != b.numInstances()) {

            throw new IllegalStateException(
                    "B+ and B row count mismatch."
            );
        }

        int smellIndex =
                requireAttribute(
                        bPlus,
                        SMELL_ATTRIBUTE
                ).index();

        int changedSmells = 0;

        for (int row = 0;
             row < bPlus.numInstances();
             row++) {

            Instance real =
                    bPlus.instance(
                            row
                    );

            Instance counterfactual =
                    b.instance(
                            row
                    );

            for (int column = 0;
                 column < bPlus.numAttributes();
                 column++) {

                if (column
                        == smellIndex) {

                    if (Double.compare(
                            real.value(column),
                            counterfactual.value(column)
                    ) != 0) {
                        changedSmells++;
                    }

                    continue;
                }

                if (!sameValue(
                        real,
                        counterfactual,
                        column
                )) {

                    throw new IllegalStateException(
                            "B differs from B+ outside NSMELLS"
                                    + " at row "
                                    + (row + 1)
                                    + ", column "
                                    + bPlus.attribute(column)
                                    .name()
                    );
                }
            }
        }

        if (changedSmells
                != EXPECTED_B_ROWS) {

            throw new IllegalStateException(
                    "Expected "
                            + EXPECTED_B_ROWS
                            + " NSMELLS changes, found "
                            + changedSmells
            );
        }
    }

    private static void validateBuggyCounts(
            Instances a,
            Instances bPlus,
            Instances b,
            Instances c
    ) {

        int aBuggy =
                countBuggyYes(
                        a
                );

        int bPlusBuggy =
                countBuggyYes(
                        bPlus
                );

        int bBuggy =
                countBuggyYes(
                        b
                );

        int cBuggy =
                countBuggyYes(
                        c
                );

        if (a.numInstances()
                != EXPECTED_A_ROWS
                || aBuggy
                != EXPECTED_A_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected A invariant."
            );
        }

        if (bPlusBuggy
                != EXPECTED_BPLUS_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected B+ BUGGY count."
            );
        }

        /*
         * B retains B+'s observed BUGGY labels only as
         * references for paired analysis.
         *
         * They are NOT interpreted as actual labels for
         * the counterfactual scenario.
         */
        if (bBuggy
                != EXPECTED_BPLUS_BUGGY) {

            throw new IllegalStateException(
                    "B did not preserve B+ reference labels."
            );
        }

        if (cBuggy
                != EXPECTED_C_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected C BUGGY count."
            );
        }

        if (bPlusBuggy
                + cBuggy
                != aBuggy) {

            throw new IllegalStateException(
                    "BUGGY partition invariant failed."
            );
        }
    }

    private static void validatePredictionCounts(
            List<PredictionRow> predictions
    ) {

        int expected =
                EXPECTED_A_ROWS
                        + EXPECTED_BPLUS_ROWS
                        + EXPECTED_B_ROWS
                        + EXPECTED_C_ROWS;

        if (predictions.size()
                != expected) {

            throw new IllegalStateException(
                    "Expected "
                            + expected
                            + " prediction rows, found "
                            + predictions.size()
            );
        }
    }

    private static int countBuggyYes(
            Instances data
    ) {

        Attribute buggy =
                requireAttribute(
                        data,
                        CLASS_ATTRIBUTE
                );

        int count = 0;

        for (Instance instance : data) {

            if ("YES".equals(
                    instance.stringValue(
                            buggy
                    )
            )) {
                count++;
            }
        }

        return count;
    }

    private static boolean sameValue(
            Instance left,
            Instance right,
            int index
    ) {

        boolean leftMissing =
                left.isMissing(
                        index
                );

        boolean rightMissing =
                right.isMissing(
                        index
                );

        if (leftMissing
                || rightMissing) {

            return leftMissing
                    && rightMissing;
        }

        Attribute attribute =
                left.dataset()
                        .attribute(
                                index
                        );

        if (attribute.isNumeric()) {

            return Double.compare(
                    left.value(
                            index
                    ),
                    right.value(
                            index
                    )
            ) == 0;
        }

        return left.toString(
                index
        ).equals(
                right.toString(
                        index
                )
        );
    }

    private static String cell(
            Instances data,
            Instance instance,
            String attributeName
    ) {

        Attribute attribute =
                requireAttribute(
                        data,
                        attributeName
                );

        if (instance.isMissing(
                attribute
        )) {
            return "";
        }

        return instance.toString(
                attribute
        );
    }

    private static Attribute requireAttribute(
            Instances data,
            String name
    ) {

        Attribute attribute =
                data.attribute(
                        name
                );

        if (attribute == null) {
            throw new IllegalStateException(
                    "Missing attribute: "
                            + name
            );
        }

        return attribute;
    }

    private static Instances loadCsv(
            Path path
    ) throws Exception {

        if (!Files.isRegularFile(
                path
        )) {
            throw new IllegalArgumentException(
                    "Dataset not found: "
                            + path.toAbsolutePath()
            );
        }

        CSVLoader loader =
                new CSVLoader();

        loader.setSource(
                path.toFile()
        );

        return loader.getDataSet();
    }

    private static void writePredictions(
            Path path,
            List<PredictionRow> rows
    ) throws Exception {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Dataset,RowIndex,Project,ReleaseIndex,"
                            + "Version,CommitId,FilePath,"
                            + "NSMELLS,ReferenceBUGGY,"
                            + "ActualAvailable,PredictedBUGGY,"
                            + "ProbabilityBUGGYYES"
            );

            writer.newLine();

            for (PredictionRow row : rows) {

                writer.write(
                        csv(
                                row.dataset()
                        )
                                + ","
                                + row.rowIndex()
                                + ","
                                + csv(
                                row.project()
                        )
                                + ","
                                + csv(
                                row.releaseIndex()
                        )
                                + ","
                                + csv(
                                row.version()
                        )
                                + ","
                                + csv(
                                row.commitId()
                        )
                                + ","
                                + csv(
                                row.filePath()
                        )
                                + ","
                                + formatDouble(
                                row.nSmells()
                        )
                                + ","
                                + csv(
                                row.referenceBuggy()
                        )
                                + ","
                                + (
                                row.actualAvailable()
                                        ? "Yes"
                                        : "No"
                        )
                                + ","
                                + csv(
                                row.predictedBuggy()
                        )
                                + ","
                                + formatDouble(
                                row.probabilityBuggyYes()
                        )
                );

                writer.newLine();
            }
        }
    }

    private static String csv(
            String value
    ) {

        if (value == null) {
            return "";
        }

        boolean quote =
                value.contains(",")
                        || value.contains("\"")
                        || value.contains("\n")
                        || value.contains("\r");

        if (!quote) {
            return value;
        }

        return "\""
                + value.replace(
                "\"",
                "\"\""
        )
                + "\"";
    }

    private static String formatDouble(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.10f",
                value
        );
    }

    private static void printSummary(
            DatasetSummary summary
    ) {

        System.out.printf(
                Locale.ROOT,
                "%-3s rows=%5d | reference BUGGY=%4d | "
                        + "predicted BUGGY=%4d | "
                        + "mean P(YES)=%.6f | actual=%s%n",
                summary.dataset(),
                summary.rows(),
                summary.referenceBuggyYes(),
                summary.predictedBuggyYes(),
                summary.meanProbabilityYes(),
                summary.actualAvailable()
                        ? "AVAILABLE"
                        : "COUNTERFACTUAL"
        );
    }

    private enum SmellMode {
        POSITIVE,
        ZERO
    }

    private record PredictionRow(
            String dataset,
            int rowIndex,
            String project,
            String releaseIndex,
            String version,
            String commitId,
            String filePath,
            double nSmells,
            String referenceBuggy,
            boolean actualAvailable,
            String predictedBuggy,
            double probabilityBuggyYes
    ) {
    }

    private record DatasetSummary(
            String dataset,
            int rows,
            boolean actualAvailable,
            int referenceBuggyYes,
            int predictedBuggyYes,
            double meanProbabilityYes
    ) {
    }
}