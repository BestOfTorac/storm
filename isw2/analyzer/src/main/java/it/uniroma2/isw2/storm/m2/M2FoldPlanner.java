package it.uniroma2.isw2.storm.m2;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public final class M2FoldPlanner {

    public static final int REPETITIONS = 10;
    public static final int FOLDS = 10;

    private static final int FIRST_SEED = 1;

    private static final String ROW_ID_ATTRIBUTE =
            "__M2_INTERNAL_ROW_ID__";

    private M2FoldPlanner() {
        // Utility class.
    }

    public static FoldPlan create(Instances modeling) {

        validateInput(modeling);

        int rows =
                modeling.numInstances();

        int[][] assignments =
                new int[REPETITIONS][rows];

        int[] seeds =
                new int[REPETITIONS];

        for (int repetition = 0;
             repetition < REPETITIONS;
             repetition++) {

            int seed =
                    FIRST_SEED + repetition;

            seeds[repetition] =
                    seed;

            Arrays.fill(
                    assignments[repetition],
                    -1
            );

            buildRepetition(
                    modeling,
                    assignments[repetition],
                    seed
            );

            validateRepetition(
                    assignments[repetition]
            );
        }

        return new FoldPlan(
                assignments,
                seeds,
                rows
        );
    }

    private static void buildRepetition(
            Instances modeling,
            int[] assignment,
            int seed
    ) {

        Instances planning =
                new Instances(modeling);

        int rowIdIndex =
                planning.numAttributes();

        planning.insertAttributeAt(
                new Attribute(ROW_ID_ATTRIBUTE),
                rowIdIndex
        );

        for (int row = 0;
             row < planning.numInstances();
             row++) {

            planning.instance(row)
                    .setValue(
                            rowIdIndex,
                            row
                    );
        }

        planning.randomize(
                new Random(seed)
        );

        planning.stratify(FOLDS);

        for (int fold = 0;
             fold < FOLDS;
             fold++) {

            Instances test =
                    planning.testCV(
                            FOLDS,
                            fold
                    );

            for (Instance instance : test) {

                int rowId =
                        (int) Math.round(
                                instance.value(
                                        rowIdIndex
                                )
                        );

                if (rowId < 0
                        || rowId >= assignment.length) {

                    throw new IllegalStateException(
                            "Invalid internal row id: "
                                    + rowId
                    );
                }

                if (assignment[rowId] != -1) {

                    throw new IllegalStateException(
                            "Row "
                                    + rowId
                                    + " assigned to multiple test folds."
                    );
                }

                assignment[rowId] =
                        fold;
            }
        }
    }

    private static void validateInput(
            Instances modeling
    ) {

        if (modeling.classIndex() < 0) {
            throw new IllegalStateException(
                    "Class attribute is not configured."
            );
        }

        if (!"BUGGY".equals(
                modeling.classAttribute().name())) {

            throw new IllegalStateException(
                    "Expected BUGGY as class attribute."
            );
        }

        if (!modeling.classAttribute().isNominal()) {
            throw new IllegalStateException(
                    "BUGGY must be nominal."
            );
        }

        if (modeling.classAttribute()
                .indexOfValue("YES") < 0
                || modeling.classAttribute()
                .indexOfValue("NO") < 0) {

            throw new IllegalStateException(
                    "BUGGY must contain YES and NO."
            );
        }

        if (modeling.attribute(
                ROW_ID_ATTRIBUTE
        ) != null) {

            throw new IllegalStateException(
                    "Internal row identifier already exists."
            );
        }
    }

    private static void validateRepetition(
            int[] assignment
    ) {

        int[] foldSizes =
                new int[FOLDS];

        for (int row = 0;
             row < assignment.length;
             row++) {

            int fold =
                    assignment[row];

            if (fold < 0 || fold >= FOLDS) {
                throw new IllegalStateException(
                        "Row "
                                + row
                                + " was not assigned exactly once."
                );
            }

            foldSizes[fold]++;
        }

        int min =
                Arrays.stream(foldSizes)
                        .min()
                        .orElseThrow();

        int max =
                Arrays.stream(foldSizes)
                        .max()
                        .orElseThrow();

        if (max - min > 1) {
            throw new IllegalStateException(
                    "Unexpected fold-size imbalance."
            );
        }
    }

    private static RepetitionStats stats(
            Instances modeling,
            int[] assignment
    ) {

        int yesIndex =
                modeling.classAttribute()
                        .indexOfValue("YES");

        int noIndex =
                modeling.classAttribute()
                        .indexOfValue("NO");

        int[] sizes =
                new int[FOLDS];

        int[] yes =
                new int[FOLDS];

        int[] no =
                new int[FOLDS];

        for (int row = 0;
             row < modeling.numInstances();
             row++) {

            int fold =
                    assignment[row];

            sizes[fold]++;

            int classValue =
                    (int) modeling.instance(row)
                            .classValue();

            if (classValue == yesIndex) {
                yes[fold]++;
            } else if (classValue == noIndex) {
                no[fold]++;
            } else {
                throw new IllegalStateException(
                        "Unexpected class value."
                );
            }
        }

        return new RepetitionStats(
                Arrays.stream(sizes).min().orElseThrow(),
                Arrays.stream(sizes).max().orElseThrow(),
                Arrays.stream(yes).min().orElseThrow(),
                Arrays.stream(yes).max().orElseThrow(),
                Arrays.stream(no).min().orElseThrow(),
                Arrays.stream(no).max().orElseThrow()
        );
    }

    private static void validateStratification(
            RepetitionStats stats
    ) {

        if (stats.maxYes() - stats.minYes() > 1) {
            throw new IllegalStateException(
                    "YES class is not properly stratified."
            );
        }

        if (stats.maxNo() - stats.minNo() > 1) {
            throw new IllegalStateException(
                    "NO class is not properly stratified."
            );
        }
    }

    public static void main(String[] args)
            throws Exception {

        Path datasetPath;

        if (args.length == 1) {

            datasetPath =
                    Path.of(args[0]);

        } else if (args.length == 0) {

            datasetPath =
                    Path.of(
                            "isw2",
                            "datasets",
                            "storm_m1_dataset_sonarcloud.csv"
                    );

        } else {

            throw new IllegalArgumentException(
                    "Usage: M2FoldPlanner [dataset.csv]"
            );
        }

        M2DatasetLoader.DatasetBundle bundle =
                M2DatasetLoader.load(
                        datasetPath
                );

        Instances modeling =
                bundle.modeling();

        FoldPlan first =
                create(modeling);

        FoldPlan second =
                create(modeling);

        if (!first.sameAssignments(second)) {
            throw new IllegalStateException(
                    "Fold planning is not reproducible."
            );
        }

        System.out.println();
        System.out.println(
                "===== M2 10x10 FOLD PLAN CHECK ====="
        );

        System.out.println(
                "Rows             : "
                        + modeling.numInstances()
        );

        System.out.println(
                "Repetitions      : "
                        + REPETITIONS
        );

        System.out.println(
                "Folds/repetition : "
                        + FOLDS
        );

        System.out.println();
        System.out.println(
                "Rep Seed FoldSize YES/test NO/test AssignmentHash"
        );

        for (int repetition = 0;
             repetition < REPETITIONS;
             repetition++) {

            RepetitionStats stats =
                    stats(
                            modeling,
                            first.assignments()[repetition]
                    );

            validateStratification(
                    stats
            );

            int hash =
                    Arrays.hashCode(
                            first.assignments()[repetition]
                    );

            System.out.printf(
                    "%3d %4d %4d-%4d %3d-%3d %4d-%4d %14d%n",
                    repetition + 1,
                    first.seeds()[repetition],
                    stats.minFoldSize(),
                    stats.maxFoldSize(),
                    stats.minYes(),
                    stats.maxYes(),
                    stats.minNo(),
                    stats.maxNo(),
                    hash
            );
        }

        System.out.println();
        System.out.println(
                "Every row once/test/repetition : YES"
        );

        System.out.println(
                "Stratification                 : OK"
        );

        System.out.println(
                "Fixed-seed reproducibility     : OK"
        );

        System.out.println(
                "Classifier-independent splits  : YES"
        );

        System.out.println();
        System.out.println(
                "RESULT: M2 FOLD PLAN OK"
        );
    }

    public record FoldPlan(
            int[][] assignments,
            int[] seeds,
            int rows
    ) {

        public int foldOf(
                int repetition,
                int row
        ) {

            return assignments[repetition][row];
        }

        public boolean sameAssignments(
                FoldPlan other
        ) {

            if (other == null
                    || assignments.length
                    != other.assignments.length) {

                return false;
            }

            for (int repetition = 0;
                 repetition < assignments.length;
                 repetition++) {

                if (!Arrays.equals(
                        assignments[repetition],
                        other.assignments[repetition])) {

                    return false;
                }
            }

            return Arrays.equals(
                    seeds,
                    other.seeds
            );
        }
    }

    private record RepetitionStats(
            int minFoldSize,
            int maxFoldSize,
            int minYes,
            int maxYes,
            int minNo,
            int maxNo
    ) {
    }
}