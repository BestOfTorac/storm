package it.uniroma2.isw2.storm.m2;

import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.Version;
import weka.filters.supervised.instance.SMOTE;

public final class WekaEnvironmentCheck {

    private static final String MAVEN_WEKA_VERSION = "3.8.7";
    private static final String EXPECTED_RUNTIME_PREFIX = "3.8.";

    private WekaEnvironmentCheck() {
        // Utility class.
    }

    public static void main(String[] args) {

        System.out.println();
        System.out.println("===== WEKA ENVIRONMENT CHECK =====");

        String actualVersion = Version.VERSION;

        System.out.println("Maven WEKA artifact   : weka-stable " + MAVEN_WEKA_VERSION);
        System.out.println("Runtime WEKA version  : " + actualVersion);

        if (!actualVersion.startsWith(EXPECTED_RUNTIME_PREFIX)) {
            throw new IllegalStateException(
                    "Unexpected WEKA runtime branch: " + actualVersion
            );
        }

        RandomForest randomForest = new RandomForest();
        NaiveBayes naiveBayes = new NaiveBayes();
        IBk ibk = new IBk();

        CfsSubsetEval cfsSubsetEval = new CfsSubsetEval();
        BestFirst bestFirst = new BestFirst();

        SMOTE smote = new SMOTE();

        printComponent(
                "RandomForest",
                randomForest
        );

        printComponent(
                "NaiveBayes",
                naiveBayes
        );

        printComponent(
                "IBk",
                ibk
        );

        printComponent(
                "CfsSubsetEval",
                cfsSubsetEval
        );

        printComponent(
                "BestFirst",
                bestFirst
        );

        printComponent(
                "SMOTE",
                smote
        );

        System.out.println();
        System.out.println("===== IMPORTANT DEFAULTS =====");

        System.out.println(
                "RandomForest trees       : "
                        + randomForest.getNumIterations()
        );

        System.out.println(
                "RandomForest seed        : "
                        + randomForest.getSeed()
        );

        System.out.println(
                "NaiveBayes kernel        : "
                        + naiveBayes.getUseKernelEstimator()
        );

        System.out.println(
                "NaiveBayes discretize    : "
                        + naiveBayes.getUseSupervisedDiscretization()
        );

        System.out.println(
                "IBk K                    : "
                        + ibk.getKNN()
        );

        System.out.println(
                "SMOTE percentage         : "
                        + smote.getPercentage()
        );

        System.out.println(
                "SMOTE neighbors          : "
                        + smote.getNearestNeighbors()
        );

        System.out.println(
                "SMOTE seed               : "
                        + smote.getRandomSeed()
        );

        System.out.println(
                "SMOTE class value        : "
                        + smote.getClassValue()
        );

        if (randomForest.getNumIterations() != 100) {
            throw new IllegalStateException(
                    "Unexpected RandomForest default number of trees."
            );
        }

        if (randomForest.getSeed() != 1) {
            throw new IllegalStateException(
                    "Unexpected RandomForest default seed."
            );
        }

        if (naiveBayes.getUseKernelEstimator()) {
            throw new IllegalStateException(
                    "NaiveBayes kernel estimator unexpectedly enabled."
            );
        }

        if (naiveBayes.getUseSupervisedDiscretization()) {
            throw new IllegalStateException(
                    "NaiveBayes supervised discretization unexpectedly enabled."
            );
        }

        if (ibk.getKNN() != 1) {
            throw new IllegalStateException(
                    "Unexpected IBk default K."
            );
        }

        if (smote.getPercentage() != 100.0) {
            throw new IllegalStateException(
                    "Unexpected SMOTE default percentage."
            );
        }

        if (smote.getNearestNeighbors() != 5) {
            throw new IllegalStateException(
                    "Unexpected SMOTE default neighbor count."
            );
        }

        if (smote.getRandomSeed() != 1) {
            throw new IllegalStateException(
                    "Unexpected SMOTE default seed."
            );
        }

        System.out.println();
        System.out.println("RESULT: WEKA ENVIRONMENT OK");
    }

    private static void printComponent(
            String name,
            Object component
    ) {

        System.out.println();
        System.out.println(name + " : OK");
        System.out.println(
                "Class   : "
                        + component.getClass().getName()
        );

        if (component instanceof OptionHandler optionHandler) {

            String options =
                    Utils.joinOptions(
                            optionHandler.getOptions()
                    );

            System.out.println(
                    "Options : "
                            + options
            );
        }
    }
}