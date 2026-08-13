package it.uniroma2.isw2.storm.m2;

import java.util.List;

import it.uniroma2.isw2.storm.m2.NpofB20Calculator.Prediction;

public final class NpofB20CalculatorCheck {

    private static final double EPSILON = 1.0e-12;

    private NpofB20CalculatorCheck() {
        // Utility class.
    }

    public static void main(String[] args) {

        System.out.println();
        System.out.println(
                "===== NPOFB20 CALCULATOR CHECK ====="
        );

        testStopsAtBudgetBoundary();

        testUsesNormalizedDefectDensity();

        testDeterministicTieBreaking();

        System.out.println();
        System.out.println(
                "RESULT: NPOFB20 CALCULATOR OK"
        );
    }

    private static void testStopsAtBudgetBoundary() {

        /*
         * Total LOC = 100
         * Budget    = 20 LOC
         *
         * Density ranking:
         *
         * row 0: 0.90 / 10 = 0.0900  BUGGY
         * row 1: 0.70 / 15 = 0.0467  CLEAN
         * row 2: 0.20 /  5 = 0.0400  BUGGY
         * row 3: 0.10 / 70 = 0.0014  CLEAN
         *
         * Inspect row 0:
         * cumulative LOC = 10
         *
         * row 1 would bring cumulative LOC to 25,
         * exceeding the 20 LOC budget.
         *
         * Inspection must STOP.
         *
         * Found buggy = 1
         * Total buggy = 2
         *
         * NPofB20 = 0.5
         */
        List<Prediction> predictions =
                List.of(
                        new Prediction(
                                0,
                                true,
                                0.90,
                                10
                        ),
                        new Prediction(
                                1,
                                false,
                                0.70,
                                15
                        ),
                        new Prediction(
                                2,
                                true,
                                0.20,
                                5
                        ),
                        new Prediction(
                                3,
                                false,
                                0.10,
                                70
                        )
                );

        double actual =
                NpofB20Calculator.calculate(
                        predictions
                );

        assertClose(
                "Budget boundary",
                0.50,
                actual
        );

        System.out.println(
                "Budget boundary          : PASS"
        );
    }

    private static void testUsesNormalizedDefectDensity() {

        /*
         * Total LOC = 100
         * Budget    = 20 LOC
         *
         * Highest raw probability:
         * row 0 = 0.95
         *
         * But normalized density:
         *
         * row 1 = 0.80 / 10 = 0.080  BUGGY
         * row 2 = 0.70 / 10 = 0.070  CLEAN
         * row 0 = 0.95 / 50 = 0.019  CLEAN
         * row 3 = 0.10 / 30 = 0.0033 CLEAN
         *
         * The first two entities consume exactly
         * the 20 LOC budget.
         *
         * The only buggy entity is found.
         *
         * NPofB20 = 1.0
         */
        List<Prediction> predictions =
                List.of(
                        new Prediction(
                                0,
                                false,
                                0.95,
                                50
                        ),
                        new Prediction(
                                1,
                                true,
                                0.80,
                                10
                        ),
                        new Prediction(
                                2,
                                false,
                                0.70,
                                10
                        ),
                        new Prediction(
                                3,
                                false,
                                0.10,
                                30
                        )
                );

        double actual =
                NpofB20Calculator.calculate(
                        predictions
                );

        assertClose(
                "Normalized ranking",
                1.00,
                actual
        );

        System.out.println(
                "Normalized ranking       : PASS"
        );
    }

    private static void testDeterministicTieBreaking() {

        /*
         * Total LOC = 50
         * Budget    = 10 LOC
         *
         * Rows 0 and 1 have identical probability,
         * LOC and defect density.
         *
         * The deterministic final tie-break is
         * original row index.
         *
         * The input list intentionally contains
         * row 1 before row 0.
         *
         * row 0 must still rank first.
         */
        List<Prediction> predictions =
                List.of(
                        new Prediction(
                                1,
                                false,
                                0.50,
                                10
                        ),
                        new Prediction(
                                0,
                                true,
                                0.50,
                                10
                        ),
                        new Prediction(
                                2,
                                false,
                                0.10,
                                30
                        )
                );

        double actual =
                NpofB20Calculator.calculate(
                        predictions
                );

        assertClose(
                "Deterministic tie",
                1.00,
                actual
        );

        System.out.println(
                "Deterministic tie        : PASS"
        );
    }

    private static void assertClose(
            String name,
            double expected,
            double actual
    ) {

        if (Math.abs(
                expected - actual
        ) > EPSILON) {

            throw new IllegalStateException(
                    name
                            + " failed: expected "
                            + expected
                            + ", found "
                            + actual
            );
        }
    }
}