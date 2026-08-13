package it.uniroma2.isw2.storm.m2;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import it.uniroma2.isw2.storm.m2.M2ClassifierFactory.ClassifierKind;
import it.uniroma2.isw2.storm.m2.NpofB20Calculator.Prediction;

public final class M2ExperimentRunner {

    private static final String DEFAULT_DATASET =
            "isw2/datasets/storm_m1_dataset_sonarcloud.csv";

    private static final String DEFAULT_RESULTS_DIR =
            "isw2/results/m2";

    private static final int SMOTE_SEED = 1;
    private static final int SMOTE_NEIGHBORS = 5;

    private M2ExperimentRunner() {
        // Utility class.
    }

    public static void main(String[] args)
            throws Exception {

        RunMode mode =
                parseMode(args);

        Path datasetPath =
                Path.of(DEFAULT_DATASET);

        Path outputDirectory =
                Path.of(DEFAULT_RESULTS_DIR);

        M2DatasetLoader.DatasetBundle bundle =
                M2DatasetLoader.load(
                        datasetPath
                );

        Instances modeling =
                bundle.modeling();

        M2FoldPlanner.FoldPlan plan =
                M2FoldPlanner.create(
                        modeling
                );

        validateLoc(modeling);

        List<MetricResult> metricResults =
                new ArrayList<>();

        List<FeatureSelectionResult> fsResults =
                new ArrayList<>();

        System.out.println();
        System.out.println(
                "===== M2 EXPERIMENT RUNNER ====="
        );

        System.out.println(
                "Mode                  : "
                        + mode.label()
        );

        System.out.println(
                "Dataset rows          : "
                        + modeling.numInstances()
        );

        System.out.println(
                "Predictors            : "
                        + (modeling.numAttributes() - 1)
        );

        System.out.println(
                "Repetitions to run    : "
                        + mode.repetitions()
        );

        System.out.println(
                "Folds/repetition      : "
                        + mode.folds()
        );

        System.out.println(
                "Classifiers           : 3"
        );

        System.out.println(
                "Configurations        : 4 (FS x Balancing)"
        );

        System.out.println();


        for (int repetition = 0;
             repetition < mode.repetitions();
             repetition++) {

            runRepetition(
                    mode,
                    repetition,
                    modeling,
                    plan,
                    metricResults,
                    fsResults
            );
        }

        validateResultCounts(
                mode,
                metricResults,
                fsResults
        );

        Files.createDirectories(
                outputDirectory
        );

        Path metricsPath =
                outputDirectory.resolve(
                        mode.metricsFileName()
                );

        Path fsPath =
                outputDirectory.resolve(
                        mode.featureSelectionFileName()
                );

        writeMetrics(
                metricsPath,
                metricResults
        );

        writeFeatureSelection(
                fsPath,
                fsResults
        );

        System.out.println();
        System.out.println(
                "===== M2 RUN COMPLETE ====="
        );

        System.out.println(
                "Metric rows           : "
                        + metricResults.size()
        );

        System.out.println(
                "FS rows               : "
                        + fsResults.size()
        );

        System.out.println(
                "Metrics CSV           : "
                        + metricsPath.toAbsolutePath()
        );

        System.out.println(
                "Feature selection CSV : "
                        + fsPath.toAbsolutePath()
        );

        System.out.println();
        System.out.println(
                "RESULT: M2 EXPERIMENT "
                        + mode.label()
                        + " OK"
        );
    }

    private static void runRepetition(
            RunMode mode,
            int repetition,
            Instances modeling,
            M2FoldPlanner.FoldPlan plan,
            List<MetricResult> metricResults,
            List<FeatureSelectionResult> fsResults
    ) throws Exception {

        int seed =
                plan.seeds()[repetition];

        Map<ExperimentKey, Evaluation> evaluations =
                new HashMap<>();

        Map<ExperimentKey, List<Prediction>> predictions =
                new HashMap<>();

        Map<ExperimentKey, boolean[]> seenRows =
                new HashMap<>();

        for (ExperimentConfig configuration
                : ExperimentConfig.values()) {

            for (ClassifierKind kind
                    : ClassifierKind.values()) {

                ExperimentKey key =
                        new ExperimentKey(
                                configuration,
                                kind
                        );

                evaluations.put(
                        key,
                        new Evaluation(modeling)
                );

                predictions.put(
                        key,
                        new ArrayList<>()
                );

                seenRows.put(
                        key,
                        new boolean[
                                modeling.numInstances()
                        ]
                );
            }
        }

        int expectedPredictions = 0;

        for (int fold = 0;
             fold < mode.folds();
             fold++) {

            PreparedFold prepared =
                    prepareFold(
                            modeling,
                            plan,
                            repetition,
                            fold
                    );

            expectedPredictions +=
                    prepared.originalTest()
                            .numInstances();

            fsResults.add(
                    new FeatureSelectionResult(
                            mode.label(),
                            repetition + 1,
                            seed,
                            fold + 1,
                            modeling.numAttributes() - 1,
                            prepared.selectedPredictors()
                                    .size(),
                            String.join(
                                    "|",
                                    prepared.selectedPredictors()
                            ),
                            prepared.trainBefore().yes(),
                            prepared.trainBefore().no(),
                            prepared.trainAfterSelectedSmote()
                                    .yes(),
                            prepared.trainAfterSelectedSmote()
                                    .no(),
                            prepared.testCounts().yes(),
                            prepared.testCounts().no(),
                            prepared.smotePercentage()
                    )
            );

            System.out.printf(
                    Locale.ROOT,
                    "[%s] repetition %d/%d, fold %d/%d"
                            + " | selected=%d/%d"
                            + " | SMOTE %.4f%%"
                            + " | test %d%n",
                    mode.label(),
                    repetition + 1,
                    mode.repetitions(),
                    fold + 1,
                    mode.folds(),
                    prepared.selectedPredictors().size(),
                    modeling.numAttributes() - 1,
                    prepared.smotePercentage(),
                    prepared.originalTest().numInstances()
            );

            for (ExperimentConfig configuration
                    : ExperimentConfig.values()) {

                FoldData foldData =
                        foldDataFor(
                                prepared,
                                configuration
                        );

                System.out.println(
                        "  "
                                + configuration.displayName()
                );

                for (ClassifierKind kind
                        : ClassifierKind.values()) {

                    ExperimentKey key =
                            new ExperimentKey(
                                    configuration,
                                    kind
                            );

                    evaluateClassifier(
                            kind,
                            modeling,
                            foldData,
                            evaluations.get(key),
                            predictions.get(key),
                            seenRows.get(key)
                    );

                    System.out.println(
                            "    "
                                    + kind.displayName()
                                    + " : OK"
                    );
                }
            }
        }

        for (ExperimentConfig configuration
                : ExperimentConfig.values()) {

            for (ClassifierKind kind
                    : ClassifierKind.values()) {

                ExperimentKey key =
                        new ExperimentKey(
                                configuration,
                                kind
                        );

                List<Prediction> classifierPredictions =
                        predictions.get(key);

                if (classifierPredictions.size()
                        != expectedPredictions) {

                    throw new IllegalStateException(
                            configuration.displayName()
                                    + " / "
                                    + kind.displayName()
                                    + ": expected "
                                    + expectedPredictions
                                    + " OOF predictions, found "
                                    + classifierPredictions.size()
                    );
                }

                if (mode.folds()
                        == M2FoldPlanner.FOLDS) {

                    validateCompleteOofCoverage(
                            configuration,
                            kind,
                            seenRows.get(key)
                    );
                }

                M2Metrics.MetricSet metrics =
                        M2Metrics.calculate(
                                evaluations.get(key),
                                bundleYesIndex(modeling),
                                classifierPredictions
                        );

                Classifier defaults =
                        M2ClassifierFactory.create(
                                kind
                        );

                metricResults.add(
                        new MetricResult(
                                mode.label(),
                                kind.displayName(),
                                configuration.featureSelection(),
                                configuration.balancing(),
                                repetition + 1,
                                seed,
                                mode.folds(),
                                classifierPredictions.size(),
                                M2ClassifierFactory.options(
                                        defaults
                                ),
                                metrics.precision(),
                                metrics.recall(),
                                metrics.auc(),
                                metrics.kappa(),
                                metrics.npofb20()
                        )
                );

                System.out.println();
                System.out.printf(
                        Locale.ROOT,
                        "%s | %s | repetition %d"
                                + " -> P=%.6f"
                                + " R=%.6f"
                                + " AUC=%.6f"
                                + " Kappa=%.6f"
                                + " NPofB20=%.6f%n",
                        configuration.displayName(),
                        kind.displayName(),
                        repetition + 1,
                        metrics.precision(),
                        metrics.recall(),
                        metrics.auc(),
                        metrics.kappa(),
                        metrics.npofb20()
                );
            }
        }

        System.out.println();
    }

    private static PreparedFold prepareFold(
            Instances modeling,
            M2FoldPlanner.FoldPlan plan,
            int repetition,
            int fold
    ) throws Exception {

        int[] assignments =
                plan.assignments()[repetition];

        Subset trainSubset =
                createSubset(
                        modeling,
                        assignments,
                        fold,
                        true
                );

        Subset testSubset =
                createSubset(
                        modeling,
                        assignments,
                        fold,
                        false
                );

        Instances train =
                trainSubset.instances();

        Instances test =
                testSubset.instances();

        if (train.numInstances()
                + test.numInstances()
                != modeling.numInstances()) {

            throw new IllegalStateException(
                    "Train/test split does not reconstruct dataset."
            );
        }

        if (!train.equalHeaders(test)) {
            throw new IllegalStateException(
                    "Train/test headers differ."
            );
        }

        ClassCounts trainBefore =
                countClasses(train);

        ClassCounts testBefore =
                countClasses(test);

        AttributeSelection featureSelection =
                new AttributeSelection();

        featureSelection.setEvaluator(
                new CfsSubsetEval()
        );

        featureSelection.setSearch(
                new BestFirst()
        );

        /*
         * IMPORTANT:
         * The filter learns ONLY from the training fold.
         */
        featureSelection.setInputFormat(
                train
        );

        Instances selectedTrain =
                Filter.useFilter(
                        train,
                        featureSelection
                );

        /*
         * Same fitted filter, no second setInputFormat().
         */
        Instances selectedTest =
                Filter.useFilter(
                        test,
                        featureSelection
                );

        configureClass(
                selectedTrain
        );

        configureClass(
                selectedTest
        );

        if (!selectedTrain.equalHeaders(
                selectedTest
        )) {

            throw new IllegalStateException(
                    "Selected train/test headers differ."
            );
        }

        if (selectedTrain.numInstances()
                != train.numInstances()) {

            throw new IllegalStateException(
                    "Feature selection changed train row count."
            );
        }

        if (selectedTest.numInstances()
                != test.numInstances()) {

            throw new IllegalStateException(
                    "Feature selection changed test row count."
            );
        }

        ClassCounts trainAfterFs =
                countClasses(
                        selectedTrain
                );

        ClassCounts testAfterFs =
                countClasses(
                        selectedTest
                );

        if (!trainBefore.equals(
                trainAfterFs
        )) {

            throw new IllegalStateException(
                    "Feature selection changed train labels."
            );
        }

        if (!testBefore.equals(
                testAfterFs
        )) {

            throw new IllegalStateException(
                    "Feature selection changed test labels."
            );
        }

        List<String> selectedPredictors =
                predictorNames(
                        selectedTrain
                );

        if (selectedPredictors.isEmpty()) {
            throw new IllegalStateException(
                    "Feature selection produced zero predictors."
            );
        }

        SmoteResult originalSmote =
                applySmote(
                        train
                );

        SmoteResult selectedSmote =
                applySmote(
                        selectedTrain
                );

        if (Math.abs(
                originalSmote.percentage()
                        - selectedSmote.percentage()
        ) > 1.0e-9) {

            throw new IllegalStateException(
                    "Unexpected SMOTE percentage mismatch."
            );
        }

        if (!originalSmote.instances()
                .equalHeaders(test)) {

            throw new IllegalStateException(
                    "SMOTE changed original-feature header."
            );
        }

        if (!selectedSmote.instances()
                .equalHeaders(selectedTest)) {

            throw new IllegalStateException(
                    "SMOTE changed selected-feature header."
            );
        }

        /*
         * Test is NEVER processed by SMOTE.
         */
        if (!testBefore.equals(
                countClasses(test)
        )
                || !testBefore.equals(
                countClasses(selectedTest)
        )) {

            throw new IllegalStateException(
                    "Test distribution changed."
            );
        }

        return new PreparedFold(
                train,
                test,
                selectedTrain,
                selectedTest,
                originalSmote.instances(),
                selectedSmote.instances(),
                testSubset.rowIndices(),
                selectedPredictors,
                trainBefore,
                selectedSmote.counts(),
                testBefore,
                selectedSmote.percentage()
        );
    }

    private static SmoteResult applySmote(
            Instances train
    ) throws Exception {

        ClassCounts before =
                countClasses(
                        train
                );

        if (before.yes()
                >= before.no()) {

            throw new IllegalStateException(
                    "BUGGY=YES is expected to be minority class."
            );
        }

        double smotePercentage =
                100.0
                        * (
                        before.no()
                                - before.yes()
                )
                        / before.yes();

        SMOTE smote =
                new SMOTE();

        /*
         * WEKA value 0 means auto-detect the non-empty
         * minority class. The check above guarantees that
         * BUGGY=YES is the minority class in this train fold.
         */
        smote.setClassValue("0");

        smote.setNearestNeighbors(
                SMOTE_NEIGHBORS
        );

        smote.setRandomSeed(
                SMOTE_SEED
        );

        smote.setPercentage(
                smotePercentage
        );

        /*
         * SMOTE is fitted and applied ONLY on train.
         */
        smote.setInputFormat(
                train
        );

        Instances balancedTrain =
                Filter.useFilter(
                        train,
                        smote
                );

        configureClass(
                balancedTrain
        );

        ClassCounts after =
                countClasses(
                        balancedTrain
                );

        if (Math.abs(
                after.yes()
                        - after.no()
        ) > 1) {

            throw new IllegalStateException(
                    "SMOTE did not balance training data."
            );
        }

        return new SmoteResult(
                balancedTrain,
                after,
                smotePercentage
        );
    }

    private static FoldData foldDataFor(
            PreparedFold prepared,
            ExperimentConfig configuration
    ) {

        Instances train;
        Instances test;

        if (configuration.featureSelection()) {

            train =
                    configuration.balancing()
                            ? prepared.balancedSelectedTrain()
                            : prepared.selectedTrain();

            test =
                    prepared.selectedTest();

        } else {

            train =
                    configuration.balancing()
                            ? prepared.balancedOriginalTrain()
                            : prepared.originalTrain();

            test =
                    prepared.originalTest();
        }

        if (!train.equalHeaders(test)) {
            throw new IllegalStateException(
                    configuration.displayName()
                            + ": train/test headers differ."
            );
        }

        return new FoldData(
                train,
                test,
                prepared.testRowIndices()
        );
    }

    private static void evaluateClassifier(
            ClassifierKind kind,
            Instances originalModeling,
            FoldData foldData,
            Evaluation evaluation,
            List<Prediction> predictions,
            boolean[] seenRows
    ) throws Exception {

        Classifier classifier =
                M2ClassifierFactory.create(
                        kind
                );

        /*
         * Give every classifier its own Instances object.
         * This keeps the four comparisons isolated even if
         * a classifier implementation modifies its input.
         */
        classifier.buildClassifier(
                new Instances(
                        foldData.train()
                )
        );

        int selectedYesIndex =
                foldData.test()
                        .classAttribute()
                        .indexOfValue("YES");

        int originalYesIndex =
                originalModeling
                        .classAttribute()
                        .indexOfValue("YES");

        if (selectedYesIndex < 0
                || originalYesIndex < 0) {

            throw new IllegalStateException(
                    "BUGGY=YES not found."
            );
        }

        validateClassOrdering(
                originalModeling,
                foldData.test()
        );

        Attribute locAttribute =
                originalModeling.attribute(
                        "LOC"
                );

        if (locAttribute == null) {
            throw new IllegalStateException(
                    "LOC not found in original modeling dataset."
            );
        }

        for (int i = 0;
             i < foldData.test().numInstances();
             i++) {

            int originalRow =
                    foldData.testRowIndices()
                            .get(i);

            if (seenRows[originalRow]) {
                throw new IllegalStateException(
                        kind.displayName()
                                + ": row "
                                + originalRow
                                + " predicted more than once."
                );
            }

            Instance transformedTestInstance =
                    foldData.test()
                            .instance(i);

            double[] distribution =
                    classifier
                            .distributionForInstance(
                                    transformedTestInstance
                            );

            if (distribution.length
                    != originalModeling
                    .classAttribute()
                    .numValues()) {

                throw new IllegalStateException(
                        "Unexpected probability distribution size."
                );
            }

            double buggyProbability =
                    distribution[
                            selectedYesIndex
                    ];

            Instance originalInstance =
                    originalModeling.instance(
                            originalRow
                    );

            /*
             * Record the prediction against the ORIGINAL
             * instance. This keeps one common Evaluation
             * header even though selected predictors can
             * differ from fold to fold.
             */
            evaluation
                    .evaluateModelOnceAndRecordPrediction(
                            distribution,
                            originalInstance
                    );

            boolean actualBuggy =
                    (int) originalInstance.classValue()
                            == originalYesIndex;

            int loc =
                    exactPositiveIntegerLoc(
                            originalInstance.value(
                                    locAttribute
                            )
                    );

            predictions.add(
                    new Prediction(
                            originalRow,
                            actualBuggy,
                            buggyProbability,
                            loc
                    )
            );

            seenRows[originalRow] =
                    true;
        }
    }

    private static Subset createSubset(
            Instances source,
            int[] assignments,
            int fold,
            boolean training
    ) {

        Instances subset =
                new Instances(
                        source,
                        0
                );

        subset.setClassIndex(
                source.classIndex()
        );

        List<Integer> rowIndices =
                new ArrayList<>();

        for (int row = 0;
             row < source.numInstances();
             row++) {

            boolean belongsToTest =
                    assignments[row] == fold;

            boolean include =
                    training
                            ? !belongsToTest
                            : belongsToTest;

            if (include) {

                subset.add(
                        (Instance) source.instance(row)
                                .copy()
                );

                rowIndices.add(
                        row
                );
            }
        }

        return new Subset(
                subset,
                List.copyOf(
                        rowIndices
                )
        );
    }

    private static void configureClass(
            Instances data
    ) {

        Attribute buggy =
                data.attribute(
                        "BUGGY"
                );

        if (buggy == null) {
            throw new IllegalStateException(
                    "BUGGY attribute missing."
            );
        }

        data.setClassIndex(
                buggy.index()
        );
    }

    private static ClassCounts countClasses(
            Instances data
    ) {

        int yesIndex =
                data.classAttribute()
                        .indexOfValue("YES");

        int noIndex =
                data.classAttribute()
                        .indexOfValue("NO");

        if (yesIndex < 0
                || noIndex < 0) {

            throw new IllegalStateException(
                    "BUGGY YES/NO not found."
            );
        }

        int yes = 0;
        int no = 0;

        for (Instance instance : data) {

            int value =
                    (int) instance.classValue();

            if (value == yesIndex) {
                yes++;
            } else if (value == noIndex) {
                no++;
            } else {
                throw new IllegalStateException(
                        "Unexpected BUGGY value."
                );
            }
        }

        return new ClassCounts(
                yes,
                no
        );
    }

    private static List<String> predictorNames(
            Instances data
    ) {

        List<String> names =
                new ArrayList<>();

        for (int index = 0;
             index < data.numAttributes();
             index++) {

            if (index != data.classIndex()) {

                names.add(
                        data.attribute(index)
                                .name()
                );
            }
        }

        return List.copyOf(
                names
        );
    }

    private static void validateClassOrdering(
            Instances original,
            Instances transformed
    ) {

        Attribute originalClass =
                original.classAttribute();

        Attribute transformedClass =
                transformed.classAttribute();

        if (originalClass.numValues()
                != transformedClass.numValues()) {

            throw new IllegalStateException(
                    "Class cardinality changed."
            );
        }

        for (int i = 0;
             i < originalClass.numValues();
             i++) {

            if (!originalClass.value(i)
                    .equals(
                            transformedClass.value(i)
                    )) {

                throw new IllegalStateException(
                        "Class value ordering changed."
                );
            }
        }
    }

    private static void validateCompleteOofCoverage(
            ExperimentConfig configuration,
            ClassifierKind kind,
            boolean[] seenRows
    ) {

        for (int row = 0;
             row < seenRows.length;
             row++) {

            if (!seenRows[row]) {

                throw new IllegalStateException(
                        configuration.displayName()
                                + " / "
                                + kind.displayName()
                                + ": row "
                                + row
                                + " has no OOF prediction."
                );
            }
        }
    }

    private static int bundleYesIndex(
            Instances modeling
    ) {

        int yesIndex =
                modeling.classAttribute()
                        .indexOfValue("YES");

        if (yesIndex < 0) {
            throw new IllegalStateException(
                    "BUGGY=YES missing."
            );
        }

        return yesIndex;
    }

    private static void validateLoc(
            Instances modeling
    ) {

        Attribute loc =
                modeling.attribute(
                        "LOC"
                );

        if (loc == null
                || !loc.isNumeric()) {

            throw new IllegalStateException(
                    "LOC must be a numeric predictor."
            );
        }

        for (Instance instance : modeling) {

            exactPositiveIntegerLoc(
                    instance.value(loc)
            );
        }
    }

    private static int exactPositiveIntegerLoc(
            double value
    ) {

        if (!Double.isFinite(value)
                || value <= 0.0) {

            throw new IllegalStateException(
                    "LOC must be finite and positive: "
                            + value
            );
        }

        long rounded =
                Math.round(value);

        if (Math.abs(
                value - rounded
        ) > 1.0e-9) {

            throw new IllegalStateException(
                    "LOC must be integer-valued: "
                            + value
            );
        }

        if (rounded > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "LOC too large: "
                            + value
            );
        }

        return (int) rounded;
    }

    private static void validateResultCounts(
            RunMode mode,
            List<MetricResult> metricResults,
            List<FeatureSelectionResult> fsResults
    ) {

        int expectedMetricRows =
                mode.repetitions()
                        * ExperimentConfig.values().length
                        * ClassifierKind.values().length;

        int expectedFsRows =
                mode.repetitions()
                        * mode.folds();

        if (metricResults.size()
                != expectedMetricRows) {

            throw new IllegalStateException(
                    "Expected "
                            + expectedMetricRows
                            + " metric rows, found "
                            + metricResults.size()
            );
        }

        if (fsResults.size()
                != expectedFsRows) {

            throw new IllegalStateException(
                    "Expected "
                            + expectedFsRows
                            + " feature-selection rows, found "
                            + fsResults.size()
            );
        }
    }

    private static String yesNo(
            boolean value
    ) {

        return value
                ? "Yes"
                : "No";
    }

    private static RunMode parseMode(
            String[] args
    ) {

        if (args.length == 0) {
            return RunMode.FULL;
        }

        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "Usage: M2ExperimentRunner"
                            + " [--smoke|--quick]"
            );
        }

        return switch (args[0]) {
            case "--smoke" ->
                    RunMode.SMOKE;

            case "--quick" ->
                    RunMode.QUICK;

            default ->
                    throw new IllegalArgumentException(
                            "Unknown mode: "
                                    + args[0]
                    );
        };
    }

    private static void writeMetrics(
            Path path,
            List<MetricResult> results
    ) throws Exception {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Mode,Classifier,FS,Balancing,"
                            + "Repetition,Seed,Folds,"
                            + "OOFPredictions,ClassifierOptions,"
                            + "Precision,Recall,AUC,Kappa,NPofB20"
            );

            writer.newLine();

            for (MetricResult result : results) {

                writer.write(
                        csv(result.mode())
                                + ","
                                + csv(result.classifier())
                                + ","
                                + csv(
                                        yesNo(
                                                result.featureSelection()
                                        )
                                )
                                + ","
                                + csv(
                                        yesNo(
                                                result.balancing()
                                        )
                                )
                                + ","
                                + result.repetition()
                                + ","
                                + result.seed()
                                + ","
                                + result.folds()
                                + ","
                                + result.oofPredictions()
                                + ","
                                + csv(result.classifierOptions())
                                + ","
                                + decimal(result.precision())
                                + ","
                                + decimal(result.recall())
                                + ","
                                + decimal(result.auc())
                                + ","
                                + decimal(result.kappa())
                                + ","
                                + decimal(result.npofb20())
                );

                writer.newLine();
            }
        }
    }

    private static void writeFeatureSelection(
            Path path,
            List<FeatureSelectionResult> results
    ) throws Exception {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "Mode,Repetition,Seed,Fold,"
                            + "InputPredictors,SelectedPredictors,"
                            + "SelectedFeatures,"
                            + "TrainYesBefore,TrainNoBefore,"
                            + "TrainYesAfterSmote,TrainNoAfterSmote,"
                            + "TestYes,TestNo,SmotePercentage"
            );

            writer.newLine();

            for (FeatureSelectionResult result
                    : results) {

                writer.write(
                        csv(result.mode())
                                + ","
                                + result.repetition()
                                + ","
                                + result.seed()
                                + ","
                                + result.fold()
                                + ","
                                + result.inputPredictors()
                                + ","
                                + result.selectedPredictors()
                                + ","
                                + csv(result.selectedFeatures())
                                + ","
                                + result.trainYesBefore()
                                + ","
                                + result.trainNoBefore()
                                + ","
                                + result.trainYesAfterSmote()
                                + ","
                                + result.trainNoAfterSmote()
                                + ","
                                + result.testYes()
                                + ","
                                + result.testNo()
                                + ","
                                + decimal(
                                        result.smotePercentage()
                                )
                );

                writer.newLine();
            }
        }
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

    private enum ExperimentConfig {

        NO_FS_NO_BALANCING(
                false,
                false
        ),

        FS_NO_BALANCING(
                true,
                false
        ),

        NO_FS_BALANCING(
                false,
                true
        ),

        FS_BALANCING(
                true,
                true
        );

        private final boolean featureSelection;
        private final boolean balancing;

        ExperimentConfig(
                boolean featureSelection,
                boolean balancing
        ) {

            this.featureSelection =
                    featureSelection;

            this.balancing =
                    balancing;
        }

        public boolean featureSelection() {
            return featureSelection;
        }

        public boolean balancing() {
            return balancing;
        }

        public String displayName() {

            return "FS="
                    + yesNo(featureSelection)
                    + " | Balancing="
                    + yesNo(balancing);
        }
    }

    private enum RunMode {

        SMOKE(
                "SMOKE",
                1,
                1,
                "classifier_metrics_smoke.csv",
                "feature_selection_smoke.csv"
        ),

        QUICK(
                "QUICK",
                1,
                M2FoldPlanner.FOLDS,
                "classifier_metrics_quick.csv",
                "feature_selection_quick.csv"
        ),

        FULL(
                "FULL",
                M2FoldPlanner.REPETITIONS,
                M2FoldPlanner.FOLDS,
                "classifier_metrics.csv",
                "feature_selection.csv"
        );

        private final String label;
        private final int repetitions;
        private final int folds;
        private final String metricsFileName;
        private final String featureSelectionFileName;

        RunMode(
                String label,
                int repetitions,
                int folds,
                String metricsFileName,
                String featureSelectionFileName
        ) {

            this.label =
                    label;

            this.repetitions =
                    repetitions;

            this.folds =
                    folds;

            this.metricsFileName =
                    metricsFileName;

            this.featureSelectionFileName =
                    featureSelectionFileName;
        }

        public String label() {
            return label;
        }

        public int repetitions() {
            return repetitions;
        }

        public int folds() {
            return folds;
        }

        public String metricsFileName() {
            return metricsFileName;
        }

        public String featureSelectionFileName() {
            return featureSelectionFileName;
        }
    }

    private record Subset(
            Instances instances,
            List<Integer> rowIndices
    ) {
    }

    private record ExperimentKey(
            ExperimentConfig configuration,
            ClassifierKind classifier
    ) {
    }

    private record PreparedFold(
            Instances originalTrain,
            Instances originalTest,
            Instances selectedTrain,
            Instances selectedTest,
            Instances balancedOriginalTrain,
            Instances balancedSelectedTrain,
            List<Integer> testRowIndices,
            List<String> selectedPredictors,
            ClassCounts trainBefore,
            ClassCounts trainAfterSelectedSmote,
            ClassCounts testCounts,
            double smotePercentage
    ) {
    }

    private record FoldData(
            Instances train,
            Instances test,
            List<Integer> testRowIndices
    ) {
    }

    private record SmoteResult(
            Instances instances,
            ClassCounts counts,
            double percentage
    ) {
    }

    private record ClassCounts(
            int yes,
            int no
    ) {
    }

    private record MetricResult(
            String mode,
            String classifier,
            boolean featureSelection,
            boolean balancing,
            int repetition,
            int seed,
            int folds,
            int oofPredictions,
            String classifierOptions,
            double precision,
            double recall,
            double auc,
            double kappa,
            double npofb20
    ) {
    }

    private record FeatureSelectionResult(
            String mode,
            int repetition,
            int seed,
            int fold,
            int inputPredictors,
            int selectedPredictors,
            String selectedFeatures,
            int trainYesBefore,
            int trainNoBefore,
            int trainYesAfterSmote,
            int trainNoAfterSmote,
            int testYes,
            int testNo,
            double smotePercentage
    ) {
    }
}