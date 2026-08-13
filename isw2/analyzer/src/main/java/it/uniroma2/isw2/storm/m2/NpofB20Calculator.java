package it.uniroma2.isw2.storm.m2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NpofB20Calculator {

    private static final double EFFORT_FRACTION = 0.20;

    private NpofB20Calculator() {
        // Utility class.
    }

    public static double calculate(
            List<Prediction> predictions
    ) {

        if (predictions == null
                || predictions.isEmpty()) {

            throw new IllegalArgumentException(
                    "Predictions must not be null or empty."
            );
        }

        long totalLoc = 0;
        int totalBuggy = 0;

        for (Prediction prediction : predictions) {

            validatePrediction(prediction);

            totalLoc += prediction.loc();

            if (prediction.actualBuggy()) {
                totalBuggy++;
            }
        }

        if (totalLoc <= 0) {
            throw new IllegalStateException(
                    "Total LOC must be positive."
            );
        }

        if (totalBuggy == 0) {
            throw new IllegalStateException(
                    "NPofB20 is undefined without buggy entities."
            );
        }

        double effortBudget =
                EFFORT_FRACTION * totalLoc;

        List<Prediction> ranking =
                new ArrayList<>(predictions);

        ranking.sort(
                Comparator
                        .<Prediction>comparingDouble(
                                Prediction::defectDensity
                        )
                        .reversed()
                        .thenComparing(
                                Comparator
                                        .comparingDouble(
                                                Prediction::buggyProbability
                                        )
                                        .reversed()
                        )
                        .thenComparingInt(
                                Prediction::rowIndex
                        )
        );

        long inspectedLoc = 0;
        int foundBuggy = 0;

        for (Prediction prediction : ranking) {

            long nextEffort =
                    inspectedLoc
                            + prediction.loc();

            /*
             * NPofB20 follows the ranking sequentially.
             *
             * If the next entity would exceed the 20% LOC
             * inspection budget, inspection stops.
             *
             * We do NOT skip that entity in search of a
             * smaller one later in the ranking.
             */
            if (nextEffort > effortBudget) {
                break;
            }

            inspectedLoc =
                    nextEffort;

            if (prediction.actualBuggy()) {
                foundBuggy++;
            }
        }

        return (double) foundBuggy
                / totalBuggy;
    }

    private static void validatePrediction(
            Prediction prediction
    ) {

        if (prediction == null) {
            throw new IllegalArgumentException(
                    "Prediction must not be null."
            );
        }

        if (prediction.rowIndex() < 0) {
            throw new IllegalArgumentException(
                    "Row index must be non-negative."
            );
        }

        if (prediction.loc() <= 0) {
            throw new IllegalArgumentException(
                    "LOC must be positive."
            );
        }

        double probability =
                prediction.buggyProbability();

        if (!Double.isFinite(probability)) {
            throw new IllegalArgumentException(
                    "Probability must be finite."
            );
        }

        if (probability < 0.0
                || probability > 1.0) {

            throw new IllegalArgumentException(
                    "Probability must be in [0,1]."
            );
        }
    }

    public record Prediction(
            int rowIndex,
            boolean actualBuggy,
            double buggyProbability,
            int loc
    ) {

        public double defectDensity() {
            return buggyProbability / loc;
        }
    }
}