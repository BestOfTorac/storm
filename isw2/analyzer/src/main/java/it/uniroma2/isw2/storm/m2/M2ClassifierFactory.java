package it.uniroma2.isw2.storm.m2;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.OptionHandler;
import weka.core.Utils;

public final class M2ClassifierFactory {

    private M2ClassifierFactory() {
        // Utility class.
    }

    public static Classifier create(
            ClassifierKind kind
    ) {

        return switch (kind) {
            case RANDOM_FOREST ->
                    new RandomForest();

            case NAIVE_BAYES ->
                    new NaiveBayes();

            case IBK ->
                    new IBk();
        };
    }

    public static String options(
            Classifier classifier
    ) {

        if (classifier instanceof OptionHandler optionHandler) {

            return Utils.joinOptions(
                    optionHandler.getOptions()
            );
        }

        return "";
    }

    public enum ClassifierKind {

        RANDOM_FOREST("RandomForest"),
        NAIVE_BAYES("NaiveBayes"),
        IBK("IBk");

        private final String displayName;

        ClassifierKind(
                String displayName
        ) {
            this.displayName =
                    displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}