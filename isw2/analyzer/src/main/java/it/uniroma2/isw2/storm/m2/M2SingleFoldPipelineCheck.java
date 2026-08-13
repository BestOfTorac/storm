package it.uniroma2.isw2.storm.m2;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

public final class M2SingleFoldPipelineCheck {

    private static final int REPETITION = 0;
    private static final int FOLD = 0;

    private M2SingleFoldPipelineCheck() {
        // Utility class.
    }

    public static void main(String[] args)
            throws Exception {

        Path datasetPath;

        if (args.length == 1) {
            datasetPath = Path.of(args[0]);
        } else if (args.length == 0) {
            datasetPath = Path.of(
                    "isw2",
                    "datasets",
                    "storm_m1_dataset_sonarcloud.csv"
            );
        } else {
            throw new IllegalArgumentException(
                    "Usage: M2SingleFoldPipelineCheck [dataset.csv]"
            );
        }

        M2DatasetLoader.DatasetBundle bundle =
                M2DatasetLoader.load(datasetPath);

        Instances modeling =
                bundle.modeling();

        M2FoldPlanner.FoldPlan plan =
                M2FoldPlanner.create(modeling);

        int[] assignments =
                plan.assignments()[REPETITION];

        Instances train =
                createSubset(
                        modeling,
                        assignments,
                        FOLD,
                        true
                );

        Instances test =
                createSubset(
                        modeling,
                        assignments,
                        FOLD,
                        false
                );

        validateInitialSplit(
                modeling,
                train,
                test
        );

        ClassCounts trainBeforeFs =
                countClasses(train);

        ClassCounts testBeforeFs =
                countClasses(test);

        /*
         * FEATURE SELECTION
         *
         * IMPORTANT:
         * setInputFormat() is called ONLY on the training fold.
         * The same fitted filter is then applied to train and test.
         */
        CfsSubsetEval evaluator =
                new CfsSubsetEval();

        BestFirst search =
                new BestFirst();

        AttributeSelection featureSelection =
                new AttributeSelection();

        featureSelection.setEvaluator(
                evaluator
        );

        featureSelection.setSearch(
                search
        );

        featureSelection.setInputFormat(
                train
        );

        Instances selectedTrain =
                Filter.useFilter(
                        train,
                        featureSelection
                );

        Instances selectedTest =
                Filter.useFilter(
                        test,
                        featureSelection
                );

        configureClass(selectedTrain);
        configureClass(selectedTest);

        validateFeatureSelection(
                train,
                test,
                selectedTrain,
                selectedTest
        );

        ClassCounts trainAfterFs =
                countClasses(selectedTrain);

        ClassCounts testAfterFs =
                countClasses(selectedTest);

        if (!trainBeforeFs.equals(trainAfterFs)) {
            throw new IllegalStateException(
                    "Feature selection changed training class counts."
            );
        }

        if (!testBeforeFs.equals(testAfterFs)) {
            throw new IllegalStateException(
                    "Feature selection changed test class counts."
            );
        }

        List<String> selectedPredictors =
                predictorNames(selectedTrain);

        /*
         * SMOTE
         *
         * Applied ONLY to the feature-selected training fold.
         * The test fold is never passed through SMOTE.
         */
        if (trainAfterFs.yes() >= trainAfterFs.no()) {
            throw new IllegalStateException(
                    "BUGGY=YES is expected to be the minority class."
            );
        }

        double smotePercentage =
                100.0
                        * (
                        trainAfterFs.no()
                                - trainAfterFs.yes()
                )
                        / trainAfterFs.yes();

        SMOTE smote =
                new SMOTE();

        smote.setClassValue("0");
        smote.setNearestNeighbors(5);
        smote.setRandomSeed(
                plan.seeds()[REPETITION]
        );
        smote.setPercentage(
                smotePercentage
        );

        smote.setInputFormat(
                selectedTrain
        );

        Instances balancedTrain =
                Filter.useFilter(
                        selectedTrain,
                        smote
                );

        configureClass(
                balancedTrain
        );

        ClassCounts trainAfterSmote =
                countClasses(
                        balancedTrain
                );

        if (Math.abs(
                trainAfterSmote.yes()
                        - trainAfterSmote.no()
        ) > 1) {

            throw new IllegalStateException(
                    "SMOTE did not balance the training fold."
            );
        }

        /*
         * The TEST fold must remain untouched by SMOTE.
         */
        ClassCounts testFinal =
                countClasses(
                        selectedTest
                );

        if (!testBeforeFs.equals(testFinal)) {
            throw new IllegalStateException(
                    "Test class distribution changed."
            );
        }

        if (selectedTest.numInstances()
                != test.numInstances()) {

            throw new IllegalStateException(
                    "Test row count changed."
            );
        }

        /*
         * DIAGNOSTIC CLASSIFIER
         *
         * RandomForest only, for now.
         * These are NOT the official M2 results.
         */
        RandomForest classifier =
                new RandomForest();

        classifier.setSeed(
                plan.seeds()[REPETITION]
        );

        classifier.buildClassifier(
                balancedTrain
        );

        Evaluation evaluation =
                new Evaluation(
                        balancedTrain
                );

        evaluation.evaluateModel(
                classifier,
                selectedTest
        );

        int yesIndex =
                selectedTest.classAttribute()
                        .indexOfValue("YES");

        System.out.println();
        System.out.println(
                "===== M2 SINGLE FOLD PIPELINE CHECK ====="
        );

        System.out.println(
                "Repetition             : "
                        + (REPETITION + 1)
        );

        System.out.println(
                "Seed                   : "
                        + plan.seeds()[REPETITION]
        );

        System.out.println(
                "Fold                   : "
                        + (FOLD + 1)
        );

        System.out.println();

        System.out.println(
                "===== ORIGINAL SPLIT ====="
        );

        System.out.println(
                "Train rows             : "
                        + train.numInstances()
        );

        System.out.println(
                "Test rows              : "
                        + test.numInstances()
        );

        System.out.println(
                "Train YES / NO         : "
                        + trainBeforeFs.yes()
                        + " / "
                        + trainBeforeFs.no()
        );

        System.out.println(
                "Test YES / NO          : "
                        + testBeforeFs.yes()
                        + " / "
                        + testBeforeFs.no()
        );

        System.out.println();

        System.out.println(
                "===== FEATURE SELECTION ====="
        );

        System.out.println(
                "Input predictors       : "
                        + (train.numAttributes() - 1)
        );

        System.out.println(
                "Selected predictors    : "
                        + selectedPredictors.size()
        );

        for (String predictor
                : selectedPredictors) {

            System.out.println(
                    "  - " + predictor
            );
        }

        System.out.println(
                "Train rows after FS    : "
                        + selectedTrain.numInstances()
        );

        System.out.println(
                "Test rows after FS     : "
                        + selectedTest.numInstances()
        );

        System.out.println(
                "Train/test headers     : "
                        + (
                        selectedTrain.equalHeaders(
                                selectedTest
                        )
                                ? "COMPATIBLE"
                                : "INCOMPATIBLE"
                )
        );

        System.out.println();

        System.out.println(
                "===== SMOTE TRAIN ONLY ====="
        );

        System.out.printf(
                "SMOTE percentage       : %.6f%%%n",
                smotePercentage
        );

        System.out.println(
                "Train before SMOTE     : "
                        + trainAfterFs.yes()
                        + " YES / "
                        + trainAfterFs.no()
                        + " NO"
        );

        System.out.println(
                "Train after SMOTE      : "
                        + trainAfterSmote.yes()
                        + " YES / "
                        + trainAfterSmote.no()
                        + " NO"
        );

        System.out.println(
                "Test after pipeline    : "
                        + testFinal.yes()
                        + " YES / "
                        + testFinal.no()
                        + " NO"
        );

        System.out.println(
                "Test SMOTE applied     : NO"
        );

        System.out.println();

        System.out.println(
                "===== RANDOM FOREST DIAGNOSTIC ====="
        );

        System.out.printf(
                "Precision YES          : %.6f%n",
                evaluation.precision(
                        yesIndex
                )
        );

        System.out.printf(
                "Recall YES             : %.6f%n",
                evaluation.recall(
                        yesIndex
                )
        );

        System.out.printf(
                "AUC YES                : %.6f%n",
                evaluation.areaUnderROC(
                        yesIndex
                )
        );

        System.out.printf(
                "Kappa                  : %.6f%n",
                evaluation.kappa()
        );

        System.out.println();
        System.out.println(
                "Leakage check          : OK"
        );

        System.out.println(
                "Feature selection      : TRAIN ONLY"
        );

        System.out.println(
                "Balancing              : TRAIN ONLY"
        );

        System.out.println(
                "Test distribution      : UNCHANGED"
        );

        System.out.println();
        System.out.println(
                "NOTE: classifier metrics above are diagnostic only."
        );

        System.out.println(
                "RESULT: M2 SINGLE FOLD PIPELINE OK"
        );
    }

    private static Instances createSubset(
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
            }
        }

        return subset;
    }

    private static void validateInitialSplit(
            Instances source,
            Instances train,
            Instances test
    ) {

        if (train.numInstances()
                + test.numInstances()
                != source.numInstances()) {

            throw new IllegalStateException(
                    "Train/test row counts do not reconstruct source."
            );
        }

        if (!train.equalHeaders(test)) {
            throw new IllegalStateException(
                    "Initial train/test headers differ."
            );
        }
    }

    private static void configureClass(
            Instances data
    ) {

        if (data.attribute("BUGGY") == null) {
            throw new IllegalStateException(
                    "BUGGY attribute missing."
            );
        }

        data.setClassIndex(
                data.attribute("BUGGY").index()
        );
    }

    private static void validateFeatureSelection(
            Instances originalTrain,
            Instances originalTest,
            Instances selectedTrain,
            Instances selectedTest
    ) {

        if (selectedTrain.numInstances()
                != originalTrain.numInstances()) {

            throw new IllegalStateException(
                    "Feature selection changed training row count."
            );
        }

        if (selectedTest.numInstances()
                != originalTest.numInstances()) {

            throw new IllegalStateException(
                    "Feature selection changed test row count."
            );
        }

        if (!selectedTrain.equalHeaders(
                selectedTest
        )) {

            throw new IllegalStateException(
                    "Selected train/test schemas are incompatible."
            );
        }

        if (!"BUGGY".equals(
                selectedTrain.classAttribute().name())) {

            throw new IllegalStateException(
                    "BUGGY lost during feature selection."
            );
        }

        if (selectedTrain.numAttributes() < 2) {
            throw new IllegalStateException(
                    "Feature selection produced no predictors."
            );
        }
    }

    private static List<String> predictorNames(
            Instances data
    ) {

        List<String> names =
                new ArrayList<>();

        for (int i = 0;
             i < data.numAttributes();
             i++) {

            if (i != data.classIndex()) {

                names.add(
                        data.attribute(i).name()
                );
            }
        }

        return List.copyOf(names);
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

        if (yesIndex < 0 || noIndex < 0) {
            throw new IllegalStateException(
                    "BUGGY values YES/NO not found."
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

    private record ClassCounts(
            int yes,
            int no
    ) {
    }
}