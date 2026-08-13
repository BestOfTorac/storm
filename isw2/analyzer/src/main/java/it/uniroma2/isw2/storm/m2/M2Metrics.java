package it.uniroma2.isw2.storm.m2;

import java.util.List;

import weka.classifiers.Evaluation;

public final class M2Metrics {

    private M2Metrics() {
        // Utility class.
    }

    public static MetricSet calculate(
            Evaluation evaluation,
            int yesIndex,
            List<NpofB20Calculator.Prediction> predictions
    ) {

        if (evaluation == null) {
            throw new IllegalArgumentException(
                    "Evaluation must not be null."
            );
        }

        if (yesIndex < 0) {
            throw new IllegalArgumentException(
                    "Invalid BUGGY=YES index."
            );
        }

        double precision =
                evaluation.precision(
                        yesIndex
                );

        double recall =
                evaluation.recall(
                        yesIndex
                );

        double auc =
                evaluation.areaUnderROC(
                        yesIndex
                );

        double kappa =
                evaluation.kappa();

        double npofb20 =
                NpofB20Calculator.calculate(
                        predictions
                );

        validateUnitMetric(
                "Precision",
                precision
        );

        validateUnitMetric(
                "Recall",
                recall
        );

        validateUnitMetric(
                "AUC",
                auc
        );

        validateKappa(
                kappa
        );

        validateUnitMetric(
                "NPofB20",
                npofb20
        );

        return new MetricSet(
                precision,
                recall,
                auc,
                kappa,
                npofb20
        );
    }

    private static void validateUnitMetric(
            String name,
            double value
    ) {

        if (!Double.isFinite(value)) {
            throw new IllegalStateException(
                    name
                            + " is not finite: "
                            + value
            );
        }

        if (value < 0.0
                || value > 1.0) {

            throw new IllegalStateException(
                    name
                            + " outside [0,1]: "
                            + value
            );
        }
    }

    private static void validateKappa(
            double value
    ) {

        if (!Double.isFinite(value)) {
            throw new IllegalStateException(
                    "Kappa is not finite: "
                            + value
            );
        }

        if (value < -1.0
                || value > 1.0) {

            throw new IllegalStateException(
                    "Kappa outside [-1,1]: "
                            + value
            );
        }
    }

    public record MetricSet(
            double precision,
            double recall,
            double auc,
            double kappa,
            double npofb20
    ) {
    }
}