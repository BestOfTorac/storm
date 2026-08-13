package it.uniroma2.isw2.storm.m2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class M2SummaryGenerator {

    private static final Path RESULTS_DIR =
            Path.of("isw2", "results", "m2");

    private static final Path METRICS_PATH =
            RESULTS_DIR.resolve("classifier_metrics.csv");

    private static final Path FEATURE_SELECTION_PATH =
            RESULTS_DIR.resolve("feature_selection.csv");

    private static final Path DATASET_PATH =
            Path.of(
                    "isw2",
                    "datasets",
                    "storm_m1_dataset_sonarcloud.csv"
            );

    private static final Path CLASSIFIER_SUMMARY_PATH =
            RESULTS_DIR.resolve("classifier_summary.csv");

    private static final Path BEST_CLASSIFIER_PATH =
            RESULTS_DIR.resolve("best_classifier.csv");

    private static final Path FEATURE_SELECTION_SUMMARY_PATH =
            RESULTS_DIR.resolve("feature_selection_summary.csv");

    private static final String METRICS_HEADER =
            "Mode,Classifier,FS,Balancing,"
                    + "Repetition,Seed,Folds,OOFPredictions,"
                    + "ClassifierOptions,"
                    + "Precision,Recall,AUC,Kappa,NPofB20";

    private static final String FEATURE_SELECTION_HEADER =
            "Mode,Repetition,Seed,Fold,"
                    + "InputPredictors,SelectedPredictors,"
                    + "SelectedFeatures,"
                    + "TrainYesBefore,TrainNoBefore,"
                    + "TrainYesAfterSmote,TrainNoAfterSmote,"
                    + "TestYes,TestNo,SmotePercentage";

    private static final int EXPECTED_METRIC_ROWS = 120;
    private static final int EXPECTED_FS_ROWS = 100;
    private static final int EXPECTED_REPETITIONS = 10;
    private static final int EXPECTED_CONFIGURATIONS = 4;
    private static final int EXPECTED_CLASSIFIERS = 3;
    private static final int EXPECTED_METRIC_CELLS =
            EXPECTED_CONFIGURATIONS * 5;

    private static final Set<String> IDENTIFIER_COLUMNS =
            Set.of(
                    "Project",
                    "ReleaseIndex",
                    "Version",
                    "CommitId",
                    "FilePath"
            );

    private M2SummaryGenerator() {
        // Utility class.
    }

    public static void main(String[] args)
            throws Exception {

        if (args.length != 0) {
            throw new IllegalArgumentException(
                    "Usage: M2SummaryGenerator"
            );
        }

        List<MetricObservation> metrics =
                readMetrics(METRICS_PATH);

        List<FeatureSelectionObservation> fs =
                readFeatureSelection(
                        FEATURE_SELECTION_PATH
                );

        validateRawInputs(
                metrics,
                fs
        );

        LinkedHashMap<SummaryKey, MetricSummary> summaries =
                summarizeMetrics(metrics);

        validateSummaries(summaries);

        BestClassifierDecision decision =
                chooseBestClassifier(summaries);

        List<String> predictors =
                readPredictorNames(DATASET_PATH);

        FeatureSelectionSummary featureSummary =
                summarizeFeatureSelection(
                        fs,
                        predictors
                );

        Files.createDirectories(RESULTS_DIR);

        writeClassifierSummary(
                CLASSIFIER_SUMMARY_PATH,
                summaries
        );

        writeBestClassifier(
                BEST_CLASSIFIER_PATH,
                decision
        );

        writeFeatureSelectionSummary(
                FEATURE_SELECTION_SUMMARY_PATH,
                featureSummary
        );

        System.out.println();
        System.out.println(
                "===== M2 SUMMARY GENERATOR ====="
        );

        System.out.println(
                "Raw metric rows          : "
                        + metrics.size()
        );

        System.out.println(
                "Classifier summary rows  : "
                        + summaries.size()
        );

        System.out.println(
                "Feature-selection folds  : "
                        + fs.size()
        );

        System.out.println(
                "Input predictors          : "
                        + predictors.size()
        );

        System.out.printf(
                Locale.ROOT,
                "Selected predictors       : min=%d mean=%.4f max=%d%n",
                featureSummary.minSelected(),
                featureSummary.meanSelected(),
                featureSummary.maxSelected()
        );

        System.out.println(
                "BClassifier               : "
                        + decision.classifier()
        );

        System.out.println(
                "Metric/config wins        : "
                        + decision.wins()
                        + "/"
                        + EXPECTED_METRIC_CELLS
        );

        System.out.println(
                "Runner-up                 : "
                        + decision.runnerUp()
                        + " ("
                        + decision.runnerUpWins()
                        + " wins)"
        );

        System.out.println();
        System.out.println(
                "Classifier summary CSV    : "
                        + CLASSIFIER_SUMMARY_PATH.toAbsolutePath()
        );

        System.out.println(
                "Best classifier CSV       : "
                        + BEST_CLASSIFIER_PATH.toAbsolutePath()
        );

        System.out.println(
                "Feature selection CSV     : "
                        + FEATURE_SELECTION_SUMMARY_PATH.toAbsolutePath()
        );

        System.out.println();
        System.out.println(
                "RESULT: M2 SUMMARY OK"
        );
    }

    private static List<MetricObservation> readMetrics(
            Path path
    ) throws IOException {

        List<List<String>> rows =
                readCsv(
                        path,
                        METRICS_HEADER
                );

        List<MetricObservation> result =
                new ArrayList<>();

        int lineNumber = 1;

        for (List<String> row : rows) {

            lineNumber++;

            if (row.size() != 14) {
                throw new IOException(
                        "Expected 14 metric columns at line "
                                + lineNumber
                                + ", found "
                                + row.size()
                );
            }

            result.add(
                    new MetricObservation(
                            row.get(0),
                            row.get(1),
                            row.get(2),
                            row.get(3),
                            parseInt(
                                    row.get(4),
                                    "Repetition",
                                    lineNumber
                            ),
                            parseInt(
                                    row.get(5),
                                    "Seed",
                                    lineNumber
                            ),
                            parseInt(
                                    row.get(6),
                                    "Folds",
                                    lineNumber
                            ),
                            parseInt(
                                    row.get(7),
                                    "OOFPredictions",
                                    lineNumber
                            ),
                            parseDouble(
                                    row.get(9),
                                    "Precision",
                                    lineNumber
                            ),
                            parseDouble(
                                    row.get(10),
                                    "Recall",
                                    lineNumber
                            ),
                            parseDouble(
                                    row.get(11),
                                    "AUC",
                                    lineNumber
                            ),
                            parseDouble(
                                    row.get(12),
                                    "Kappa",
                                    lineNumber
                            ),
                            parseDouble(
                                    row.get(13),
                                    "NPofB20",
                                    lineNumber
                            )
                    )
            );
        }

        return List.copyOf(result);
    }

    private static List<FeatureSelectionObservation>
    readFeatureSelection(
            Path path
    ) throws IOException {

        List<List<String>> rows =
                readCsv(
                        path,
                        FEATURE_SELECTION_HEADER
                );

        List<FeatureSelectionObservation> result =
                new ArrayList<>();

        int lineNumber = 1;

        for (List<String> row : rows) {

            lineNumber++;

            if (row.size() != 14) {
                throw new IOException(
                        "Expected 14 feature-selection columns at line "
                                + lineNumber
                                + ", found "
                                + row.size()
                );
            }

            result.add(
                    new FeatureSelectionObservation(
                            row.get(0),
                            parseInt(
                                    row.get(1),
                                    "Repetition",
                                    lineNumber
                            ),
                            parseInt(
                                    row.get(2),
                                    "Seed",
                                    lineNumber
                            ),
                            parseInt(
                                    row.get(3),
                                    "Fold",
                                    lineNumber
                            ),
                            parseInt(
                                    row.get(4),
                                    "InputPredictors",
                                    lineNumber
                            ),
                            parseInt(
                                    row.get(5),
                                    "SelectedPredictors",
                                    lineNumber
                            ),
                            row.get(6)
                    )
            );
        }

        return List.copyOf(result);
    }

    private static List<List<String>> readCsv(
            Path path,
            String expectedHeader
    ) throws IOException {

        if (!Files.isRegularFile(path)) {
            throw new IOException(
                    "CSV not found: "
                            + path
            );
        }

        List<List<String>> rows =
                new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            String header =
                    stripBom(
                            reader.readLine()
                    );

            if (!expectedHeader.equals(header)) {
                throw new IOException(
                        "Unexpected CSV header in "
                                + path
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                if (line.isBlank()) {
                    continue;
                }

                rows.add(
                        parseCsvLine(
                                line,
                                lineNumber
                        )
                );
            }
        }

        return List.copyOf(rows);
    }

    private static void validateRawInputs(
            List<MetricObservation> metrics,
            List<FeatureSelectionObservation> fs
    ) {

        if (metrics.size()
                != EXPECTED_METRIC_ROWS) {

            throw new IllegalStateException(
                    "Expected "
                            + EXPECTED_METRIC_ROWS
                            + " metric rows, found "
                            + metrics.size()
            );
        }

        if (fs.size()
                != EXPECTED_FS_ROWS) {

            throw new IllegalStateException(
                    "Expected "
                            + EXPECTED_FS_ROWS
                            + " feature-selection rows, found "
                            + fs.size()
            );
        }

        for (MetricObservation observation : metrics) {

            if (!"FULL".equals(
                    observation.mode()
            )) {

                throw new IllegalStateException(
                        "classifier_metrics.csv must contain only FULL rows."
                );
            }

            if (observation.folds() != 10) {
                throw new IllegalStateException(
                        "Unexpected fold count."
                );
            }

            if (observation.oofPredictions()
                    != 14611) {

                throw new IllegalStateException(
                        "Unexpected OOF prediction count."
                );
            }

            if (observation.repetition() < 1
                    || observation.repetition()
                    > EXPECTED_REPETITIONS) {

                throw new IllegalStateException(
                        "Invalid repetition."
                );
            }

            validateYesNo(
                    observation.fs(),
                    "FS"
            );

            validateYesNo(
                    observation.balancing(),
                    "Balancing"
            );
        }

        for (FeatureSelectionObservation observation : fs) {

            if (!"FULL".equals(
                    observation.mode()
            )) {

                throw new IllegalStateException(
                        "feature_selection.csv must contain only FULL rows."
                );
            }

            if (observation.inputPredictors() <= 0) {
                throw new IllegalStateException(
                        "Invalid InputPredictors."
                );
            }

            if (observation.selectedPredictors() <= 0
                    || observation.selectedPredictors()
                    > observation.inputPredictors()) {

                throw new IllegalStateException(
                        "Invalid SelectedPredictors."
                );
            }
        }
    }

    private static LinkedHashMap<SummaryKey, MetricSummary>
    summarizeMetrics(
            List<MetricObservation> metrics
    ) {

        LinkedHashMap<SummaryKey, List<MetricObservation>> groups =
                new LinkedHashMap<>();

        for (MetricObservation observation : metrics) {

            SummaryKey key =
                    new SummaryKey(
                            observation.classifier(),
                            observation.fs(),
                            observation.balancing()
                    );

            groups.computeIfAbsent(
                    key,
                    ignored -> new ArrayList<>()
            ).add(observation);
        }

        LinkedHashMap<SummaryKey, MetricSummary> summaries =
                new LinkedHashMap<>();

        for (Map.Entry<SummaryKey, List<MetricObservation>> entry
                : groups.entrySet()) {

            List<MetricObservation> group =
                    entry.getValue();

            summaries.put(
                    entry.getKey(),
                    new MetricSummary(
                            group.size(),
                            stats(
                                    group.stream()
                                            .mapToDouble(
                                                    MetricObservation::precision
                                            )
                                            .toArray()
                            ),
                            stats(
                                    group.stream()
                                            .mapToDouble(
                                                    MetricObservation::recall
                                            )
                                            .toArray()
                            ),
                            stats(
                                    group.stream()
                                            .mapToDouble(
                                                    MetricObservation::auc
                                            )
                                            .toArray()
                            ),
                            stats(
                                    group.stream()
                                            .mapToDouble(
                                                    MetricObservation::kappa
                                            )
                                            .toArray()
                            ),
                            stats(
                                    group.stream()
                                            .mapToDouble(
                                                    MetricObservation::npofb20
                                            )
                                            .toArray()
                            )
                    )
            );
        }

        return summaries;
    }

    private static void validateSummaries(
            Map<SummaryKey, MetricSummary> summaries
    ) {

        int expected =
                EXPECTED_CONFIGURATIONS
                        * EXPECTED_CLASSIFIERS;

        if (summaries.size() != expected) {
            throw new IllegalStateException(
                    "Expected "
                            + expected
                            + " summary groups, found "
                            + summaries.size()
            );
        }

        Set<String> classifiers =
                new LinkedHashSet<>();

        Set<String> configurations =
                new LinkedHashSet<>();

        for (Map.Entry<SummaryKey, MetricSummary> entry
                : summaries.entrySet()) {

            if (entry.getValue().repetitions()
                    != EXPECTED_REPETITIONS) {

                throw new IllegalStateException(
                        entry.getKey()
                                + " has "
                                + entry.getValue().repetitions()
                                + " repetitions."
                );
            }

            classifiers.add(
                    entry.getKey().classifier()
            );

            configurations.add(
                    entry.getKey().fs()
                            + "|"
                            + entry.getKey().balancing()
            );
        }

        if (classifiers.size()
                != EXPECTED_CLASSIFIERS) {

            throw new IllegalStateException(
                    "Expected 3 classifiers."
            );
        }

        if (configurations.size()
                != EXPECTED_CONFIGURATIONS) {

            throw new IllegalStateException(
                    "Expected 4 FS/Balancing configurations."
            );
        }
    }

    private static BestClassifierDecision
    chooseBestClassifier(
            Map<SummaryKey, MetricSummary> summaries
    ) {

        LinkedHashSet<String> classifiers =
                new LinkedHashSet<>();

        LinkedHashSet<ConfigurationKey> configurations =
                new LinkedHashSet<>();

        for (SummaryKey key : summaries.keySet()) {

            classifiers.add(
                    key.classifier()
            );

            configurations.add(
                    new ConfigurationKey(
                            key.fs(),
                            key.balancing()
                    )
            );
        }

        Map<String, Integer> wins =
                new LinkedHashMap<>();

        for (String classifier : classifiers) {
            wins.put(classifier, 0);
        }

        for (ConfigurationKey configuration
                : configurations) {

            for (int metricIndex = 0;
                 metricIndex < 5;
                 metricIndex++) {

                String winner = null;
                double best = Double.NEGATIVE_INFINITY;
                boolean tie = false;

                for (String classifier
                        : classifiers) {

                    MetricSummary summary =
                            summaries.get(
                                    new SummaryKey(
                                            classifier,
                                            configuration.fs(),
                                            configuration.balancing()
                                    )
                            );

                    if (summary == null) {
                        throw new IllegalStateException(
                                "Missing summary group."
                        );
                    }

                    double value =
                            metricMean(
                                    summary,
                                    metricIndex
                            );

                    if (value > best + 1.0e-12) {

                        best = value;
                        winner = classifier;
                        tie = false;

                    } else if (Math.abs(
                            value - best
                    ) <= 1.0e-12) {

                        tie = true;
                    }
                }

                if (tie) {
                    throw new IllegalStateException(
                            "Metric-cell tie requires manual review."
                    );
                }

                wins.put(
                        winner,
                        wins.get(winner) + 1
                );
            }
        }

        List<Map.Entry<String, Integer>> ranking =
                new ArrayList<>(
                        wins.entrySet()
                );

        ranking.sort(
                Map.Entry.<String, Integer>
                        comparingByValue()
                        .reversed()
                        .thenComparing(
                                Map.Entry::getKey
                        )
        );

        if (ranking.size() < 2) {
            throw new IllegalStateException(
                    "Not enough classifiers."
            );
        }

        if (ranking.get(0).getValue()
                .equals(
                        ranking.get(1).getValue()
                )) {

            throw new IllegalStateException(
                    "BClassifier selection tie requires manual review."
            );
        }

        String selected =
                ranking.get(0).getKey();

        String runnerUp =
                ranking.get(1).getKey();

        return new BestClassifierDecision(
                selected,
                ranking.get(0).getValue(),
                runnerUp,
                ranking.get(1).getValue(),
                overallMetricMeans(
                        selected,
                        summaries
                )
        );
    }

    private static double[] overallMetricMeans(
            String classifier,
            Map<SummaryKey, MetricSummary> summaries
    ) {

        double[] sums =
                new double[5];

        int count = 0;

        for (Map.Entry<SummaryKey, MetricSummary> entry
                : summaries.entrySet()) {

            if (!classifier.equals(
                    entry.getKey().classifier()
            )) {
                continue;
            }

            for (int metricIndex = 0;
                 metricIndex < 5;
                 metricIndex++) {

                sums[metricIndex] +=
                        metricMean(
                                entry.getValue(),
                                metricIndex
                        );
            }

            count++;
        }

        if (count != EXPECTED_CONFIGURATIONS) {
            throw new IllegalStateException(
                    "Expected 4 configurations for "
                            + classifier
            );
        }

        for (int metricIndex = 0;
             metricIndex < sums.length;
             metricIndex++) {

            sums[metricIndex] /=
                    count;
        }

        return sums;
    }

    private static double metricMean(
            MetricSummary summary,
            int metricIndex
    ) {

        return switch (metricIndex) {
            case 0 -> summary.precision().mean();
            case 1 -> summary.recall().mean();
            case 2 -> summary.auc().mean();
            case 3 -> summary.kappa().mean();
            case 4 -> summary.npofb20().mean();

            default ->
                    throw new IllegalArgumentException(
                            "Unknown metric index."
                    );
        };
    }

    private static List<String> readPredictorNames(
            Path datasetPath
    ) throws IOException {

        if (!Files.isRegularFile(datasetPath)) {
            throw new IOException(
                    "Dataset not found: "
                            + datasetPath
            );
        }

        String header;

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             datasetPath,
                             StandardCharsets.UTF_8
                     )) {

            header =
                    stripBom(
                            reader.readLine()
                    );
        }

        if (header == null
                || header.isBlank()) {

            throw new IOException(
                    "Dataset header is empty."
            );
        }

        List<String> columns =
                parseCsvLine(
                        header,
                        1
                );

        List<String> predictors =
                new ArrayList<>();

        for (String column : columns) {

            if (IDENTIFIER_COLUMNS.contains(column)
                    || "BUGGY".equals(column)) {

                continue;
            }

            predictors.add(column);
        }

        if (predictors.size() != 24) {
            throw new IOException(
                    "Expected 24 predictors in Dataset A, found "
                            + predictors.size()
            );
        }

        if (!predictors.contains("NSMELLS")) {
            throw new IOException(
                    "NSMELLS missing from Dataset A predictors."
            );
        }

        return List.copyOf(predictors);
    }

    private static FeatureSelectionSummary
    summarizeFeatureSelection(
            List<FeatureSelectionObservation> observations,
            List<String> predictors
    ) {

        Map<String, Integer> frequencies =
                new LinkedHashMap<>();

        for (String predictor : predictors) {
            frequencies.put(predictor, 0);
        }

        int minSelected =
                Integer.MAX_VALUE;

        int maxSelected =
                Integer.MIN_VALUE;

        long totalSelected = 0L;

        int expectedInputPredictors =
                predictors.size();

        for (FeatureSelectionObservation observation
                : observations) {

            if (observation.inputPredictors()
                    != expectedInputPredictors) {

                throw new IllegalStateException(
                        "Unexpected InputPredictors at repetition "
                                + observation.repetition()
                                + ", fold "
                                + observation.fold()
                );
            }

            minSelected =
                    Math.min(
                            minSelected,
                            observation.selectedPredictors()
                    );

            maxSelected =
                    Math.max(
                            maxSelected,
                            observation.selectedPredictors()
                    );

            totalSelected +=
                    observation.selectedPredictors();

            String[] selected =
                    observation.selectedFeatures()
                            .split("\\|");

            Set<String> unique =
                    new LinkedHashSet<>();

            for (String feature : selected) {

                if (feature.isBlank()) {
                    continue;
                }

                if (!frequencies.containsKey(feature)) {
                    throw new IllegalStateException(
                            "Unknown selected feature: "
                                    + feature
                    );
                }

                if (!unique.add(feature)) {
                    throw new IllegalStateException(
                            "Feature selected twice in one fold: "
                                    + feature
                    );
                }
            }

            if (unique.size()
                    != observation.selectedPredictors()) {

                throw new IllegalStateException(
                        "Selected feature count mismatch at repetition "
                                + observation.repetition()
                                + ", fold "
                                + observation.fold()
                );
            }

            for (String feature : unique) {

                frequencies.put(
                        feature,
                        frequencies.get(feature) + 1
                );
            }
        }

        long frequencyTotal =
                frequencies.values()
                        .stream()
                        .mapToLong(
                                Integer::longValue
                        )
                        .sum();

        if (frequencyTotal != totalSelected) {
            throw new IllegalStateException(
                    "Feature-frequency total does not match "
                            + "SelectedPredictors total."
            );
        }

        double meanSelected =
                (double) totalSelected
                        / observations.size();

        List<FeatureFrequency> ordered =
                new ArrayList<>();

        for (Map.Entry<String, Integer> entry
                : frequencies.entrySet()) {

            ordered.add(
                    new FeatureFrequency(
                            entry.getKey(),
                            entry.getValue(),
                            (double) entry.getValue()
                                    / observations.size()
                    )
            );
        }

        ordered.sort(
                Comparator.comparingInt(
                                FeatureFrequency::selectedFolds
                        )
                        .reversed()
                        .thenComparing(
                                FeatureFrequency::feature
                        )
        );

        return new FeatureSelectionSummary(
                observations.size(),
                predictors.size(),
                minSelected,
                meanSelected,
                maxSelected,
                List.copyOf(ordered)
        );
    }

    private static Stats stats(
            double[] values
    ) {

        if (values.length < 2) {
            throw new IllegalStateException(
                    "At least two values are required "
                            + "for sample standard deviation."
            );
        }

        double sum = 0.0;

        for (double value : values) {

            if (!Double.isFinite(value)) {
                throw new IllegalStateException(
                        "Non-finite metric value."
                );
            }

            sum += value;
        }

        double mean =
                sum / values.length;

        double squaredDifferenceSum =
                0.0;

        for (double value : values) {

            double difference =
                    value - mean;

            squaredDifferenceSum +=
                    difference * difference;
        }

        double sampleStd =
                Math.sqrt(
                        squaredDifferenceSum
                                / (values.length - 1)
                );

        return new Stats(
                mean,
                sampleStd
        );
    }

    private static void writeClassifierSummary(
            Path path,
            Map<SummaryKey, MetricSummary> summaries
    ) throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Dataset,Classifier,FS,Balancing,Repetitions,"
                            + "PrecisionMean,PrecisionStd,"
                            + "RecallMean,RecallStd,"
                            + "AUCMean,AUCStd,"
                            + "KappaMean,KappaStd,"
                            + "NPofB20Mean,NPofB20Std"
            );

            writer.newLine();

            for (Map.Entry<SummaryKey, MetricSummary> entry
                    : summaries.entrySet()) {

                SummaryKey key =
                        entry.getKey();

                MetricSummary summary =
                        entry.getValue();

                writer.write(
                        csv("STORM")
                                + ","
                                + csv(key.classifier())
                                + ","
                                + csv(key.fs())
                                + ","
                                + csv(key.balancing())
                                + ","
                                + summary.repetitions()
                                + ","
                                + decimal(summary.precision().mean())
                                + ","
                                + decimal(summary.precision().std())
                                + ","
                                + decimal(summary.recall().mean())
                                + ","
                                + decimal(summary.recall().std())
                                + ","
                                + decimal(summary.auc().mean())
                                + ","
                                + decimal(summary.auc().std())
                                + ","
                                + decimal(summary.kappa().mean())
                                + ","
                                + decimal(summary.kappa().std())
                                + ","
                                + decimal(summary.npofb20().mean())
                                + ","
                                + decimal(summary.npofb20().std())
                );

                writer.newLine();
            }
        }
    }

    private static void writeBestClassifier(
            Path path,
            BestClassifierDecision decision
    ) throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Dataset,BClassifier,"
                            + "WinningMetricCells,TotalMetricCells,"
                            + "RunnerUp,RunnerUpWinningMetricCells,"
                            + "PrecisionOverallMean,"
                            + "RecallOverallMean,"
                            + "AUCOverallMean,"
                            + "KappaOverallMean,"
                            + "NPofB20OverallMean,"
                            + "SelectionCriterion"
            );

            writer.newLine();

            double[] means =
                    decision.overallMeans();

            writer.write(
                    csv("STORM")
                            + ","
                            + csv(decision.classifier())
                            + ","
                            + decision.wins()
                            + ","
                            + EXPECTED_METRIC_CELLS
                            + ","
                            + csv(decision.runnerUp())
                            + ","
                            + decision.runnerUpWins()
                            + ","
                            + decimal(means[0])
                            + ","
                            + decimal(means[1])
                            + ","
                            + decimal(means[2])
                            + ","
                            + decimal(means[3])
                            + ","
                            + decimal(means[4])
                            + ","
                            + csv(
                                    "Highest number of mean-metric wins "
                                            + "across the 4 FS/Balancing "
                                            + "configurations and 5 M2 metrics; "
                                            + "ties require manual review"
                            )
            );

            writer.newLine();
        }
    }

    private static void writeFeatureSelectionSummary(
            Path path,
            FeatureSelectionSummary summary
    ) throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Dataset,Feature,"
                            + "SelectedFolds,TotalFolds,"
                            + "SelectionFrequency,"
                            + "InputPredictors,"
                            + "MinSelectedPredictors,"
                            + "MeanSelectedPredictors,"
                            + "MaxSelectedPredictors"
            );

            writer.newLine();

            for (FeatureFrequency frequency
                    : summary.frequencies()) {

                writer.write(
                        csv("STORM")
                                + ","
                                + csv(frequency.feature())
                                + ","
                                + frequency.selectedFolds()
                                + ","
                                + summary.totalFolds()
                                + ","
                                + decimal(
                                        frequency.frequency()
                                )
                                + ","
                                + summary.inputPredictors()
                                + ","
                                + summary.minSelected()
                                + ","
                                + decimal(
                                        summary.meanSelected()
                                )
                                + ","
                                + summary.maxSelected()
                );

                writer.newLine();
            }
        }
    }

    private static List<String> parseCsvLine(
            String line,
            int lineNumber
    ) throws IOException {

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted = false;

        for (int index = 0;
             index < line.length();
             index++) {

            char character =
                    line.charAt(index);

            if (quoted) {

                if (character == '"') {

                    if (index + 1 < line.length()
                            && line.charAt(index + 1) == '"') {

                        current.append('"');
                        index++;

                    } else {

                        quoted = false;
                    }

                } else {

                    current.append(character);
                }

            } else {

                if (character == ',') {

                    values.add(
                            current.toString()
                    );

                    current.setLength(0);

                } else if (character == '"') {

                    if (current.length() != 0) {
                        throw new IOException(
                                "Unexpected quote at line "
                                        + lineNumber
                        );
                    }

                    quoted = true;

                } else {

                    current.append(character);
                }
            }
        }

        if (quoted) {
            throw new IOException(
                    "Unclosed quoted field at line "
                            + lineNumber
            );
        }

        values.add(
                current.toString()
        );

        return List.copyOf(values);
    }

    private static int parseInt(
            String value,
            String column,
            int lineNumber
    ) throws IOException {

        try {

            return Integer.parseInt(value);

        } catch (NumberFormatException exception) {

            throw new IOException(
                    "Invalid "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + value,
                    exception
            );
        }
    }

    private static double parseDouble(
            String value,
            String column,
            int lineNumber
    ) throws IOException {

        try {

            double parsed =
                    Double.parseDouble(value);

            if (!Double.isFinite(parsed)) {
                throw new NumberFormatException(
                        "Non-finite value."
                );
            }

            return parsed;

        } catch (NumberFormatException exception) {

            throw new IOException(
                    "Invalid "
                            + column
                            + " at line "
                            + lineNumber
                            + ": "
                            + value,
                    exception
            );
        }
    }

    private static void validateYesNo(
            String value,
            String column
    ) {

        if (!"Yes".equals(value)
                && !"No".equals(value)) {

            throw new IllegalStateException(
                    column
                            + " must be Yes or No: "
                            + value
            );
        }
    }

    private static String stripBom(
            String value
    ) {

        if (value != null
                && value.startsWith("\uFEFF")) {

            return value.substring(1);
        }

        return value;
    }

    private static String decimal(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.10f",
                value
        );
    }

    private static String csv(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return "\""
                + value.replace(
                        "\"",
                        "\"\""
                )
                + "\"";
    }

    private record MetricObservation(
            String mode,
            String classifier,
            String fs,
            String balancing,
            int repetition,
            int seed,
            int folds,
            int oofPredictions,
            double precision,
            double recall,
            double auc,
            double kappa,
            double npofb20
    ) {
    }

    private record FeatureSelectionObservation(
            String mode,
            int repetition,
            int seed,
            int fold,
            int inputPredictors,
            int selectedPredictors,
            String selectedFeatures
    ) {
    }

    private record SummaryKey(
            String classifier,
            String fs,
            String balancing
    ) {
    }

    private record ConfigurationKey(
            String fs,
            String balancing
    ) {
    }

    private record Stats(
            double mean,
            double std
    ) {
    }

    private record MetricSummary(
            int repetitions,
            Stats precision,
            Stats recall,
            Stats auc,
            Stats kappa,
            Stats npofb20
    ) {
    }

    private record BestClassifierDecision(
            String classifier,
            int wins,
            String runnerUp,
            int runnerUpWins,
            double[] overallMeans
    ) {
    }

    private record FeatureFrequency(
            String feature,
            int selectedFolds,
            double frequency
    ) {
    }

    private record FeatureSelectionSummary(
            int totalFolds,
            int inputPredictors,
            int minSelected,
            double meanSelected,
            int maxSelected,
            List<FeatureFrequency> frequencies
    ) {
    }
}
