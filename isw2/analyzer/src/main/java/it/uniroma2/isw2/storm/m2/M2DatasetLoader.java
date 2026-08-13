package it.uniroma2.isw2.storm.m2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public final class M2DatasetLoader {

    private static final int EXPECTED_INSTANCES = 14_611;
    private static final int EXPECTED_RAW_ATTRIBUTES = 30;
    private static final int EXPECTED_METADATA_ATTRIBUTES = 5;
    private static final int EXPECTED_PREDICTORS = 24;

    private static final int EXPECTED_BUGGY_YES = 1_243;
    private static final int EXPECTED_BUGGY_NO = 13_368;

    private static final List<String> METADATA_ATTRIBUTES = List.of(
            "Project",
            "ReleaseIndex",
            "Version",
            "CommitId",
            "FilePath"
    );

    private static final List<String> PREDICTOR_ATTRIBUTES = List.of(
            "LOC",
            "LOC_TOUCHED",
            "NR",
            "NFIX",
            "NAUTH",
            "LOC_ADDED",
            "MAX_LOC_ADDED",
            "AVG_LOC_ADDED",
            "CHURN",
            "MAX_CHURN",
            "AVG_CHURN",
            "CHANGE_SET_SIZE",
            "MAX_CHANGE_SET_SIZE",
            "AVG_CHANGE_SET_SIZE",
            "AGE_WEEKS",
            "WEIGHTED_AGE_WEEKS",
            "LOC_DELETED",
            "MAX_LOC_DELETED",
            "AVG_LOC_DELETED",
            "AVG_ND",
            "MAX_ND",
            "AVG_ENTROPY",
            "MAX_ENTROPY",
            "NSMELLS"
    );

    private M2DatasetLoader() {
        // Utility class.
    }

    public static DatasetBundle load(Path csvPath) throws Exception {

        if (!Files.isRegularFile(csvPath)) {
            throw new IllegalArgumentException(
                    "Dataset not found: " + csvPath.toAbsolutePath()
            );
        }

        CSVLoader loader = new CSVLoader();
        loader.setSource(csvPath.toFile());

        Instances raw = loader.getDataSet();

        validateRawDataset(raw);

        Remove removeMetadata = new Remove();
        removeMetadata.setAttributeIndices("1-5");
        removeMetadata.setInputFormat(raw);

        Instances modeling =
                Filter.useFilter(raw, removeMetadata);

        modeling.setClassIndex(
                modeling.numAttributes() - 1
        );

        ValidationSummary summary =
                validateModelingDataset(modeling);

        return new DatasetBundle(
                raw,
                modeling,
                summary.yesIndex(),
                summary.noIndex(),
                summary.yesCount(),
                summary.noCount(),
                summary.missingValues()
        );
    }

    private static void validateRawDataset(Instances raw) {

        if (raw.numInstances() != EXPECTED_INSTANCES) {
            throw new IllegalStateException(
                    "Unexpected number of rows: "
                            + raw.numInstances()
            );
        }

        if (raw.numAttributes() != EXPECTED_RAW_ATTRIBUTES) {
            throw new IllegalStateException(
                    "Unexpected number of raw attributes: "
                            + raw.numAttributes()
            );
        }

        for (int i = 0; i < METADATA_ATTRIBUTES.size(); i++) {

            String expected =
                    METADATA_ATTRIBUTES.get(i);

            String actual =
                    raw.attribute(i).name();

            if (!expected.equals(actual)) {
                throw new IllegalStateException(
                        "Unexpected metadata attribute at position "
                                + (i + 1)
                                + ": expected "
                                + expected
                                + ", found "
                                + actual
                );
            }
        }

        for (int i = 0; i < PREDICTOR_ATTRIBUTES.size(); i++) {

            int rawIndex =
                    EXPECTED_METADATA_ATTRIBUTES + i;

            String expected =
                    PREDICTOR_ATTRIBUTES.get(i);

            String actual =
                    raw.attribute(rawIndex).name();

            if (!expected.equals(actual)) {
                throw new IllegalStateException(
                        "Unexpected predictor at position "
                                + (rawIndex + 1)
                                + ": expected "
                                + expected
                                + ", found "
                                + actual
                );
            }
        }

        String targetName =
                raw.attribute(
                        raw.numAttributes() - 1
                ).name();

        if (!"BUGGY".equals(targetName)) {
            throw new IllegalStateException(
                    "Expected BUGGY as last attribute, found: "
                            + targetName
            );
        }
    }

    private static ValidationSummary validateModelingDataset(
            Instances modeling
    ) {

        int expectedModelAttributes =
                EXPECTED_PREDICTORS + 1;

        if (modeling.numInstances() != EXPECTED_INSTANCES) {
            throw new IllegalStateException(
                    "Row count changed after metadata removal."
            );
        }

        if (modeling.numAttributes() != expectedModelAttributes) {
            throw new IllegalStateException(
                    "Unexpected modeling attribute count: "
                            + modeling.numAttributes()
            );
        }

        if (modeling.classIndex()
                != modeling.numAttributes() - 1) {

            throw new IllegalStateException(
                    "BUGGY is not the last/class attribute."
            );
        }

        Attribute classAttribute =
                modeling.classAttribute();

        if (!"BUGGY".equals(classAttribute.name())) {
            throw new IllegalStateException(
                    "Unexpected class attribute: "
                            + classAttribute.name()
            );
        }

        if (!classAttribute.isNominal()) {
            throw new IllegalStateException(
                    "BUGGY must be nominal."
            );
        }

        for (int i = 0; i < EXPECTED_PREDICTORS; i++) {

            Attribute attribute =
                    modeling.attribute(i);

            String expected =
                    PREDICTOR_ATTRIBUTES.get(i);

            if (!expected.equals(attribute.name())) {
                throw new IllegalStateException(
                        "Unexpected modeling predictor at position "
                                + (i + 1)
                                + ": expected "
                                + expected
                                + ", found "
                                + attribute.name()
                );
            }

            if (!attribute.isNumeric()) {
                throw new IllegalStateException(
                        "Predictor is not numeric: "
                                + attribute.name()
                );
            }
        }

        for (String metadata : METADATA_ATTRIBUTES) {

            if (modeling.attribute(metadata) != null) {
                throw new IllegalStateException(
                        "Metadata leaked into modeling dataset: "
                                + metadata
                );
            }
        }

        int yesIndex =
                classAttribute.indexOfValue("YES");

        int noIndex =
                classAttribute.indexOfValue("NO");

        if (yesIndex < 0 || noIndex < 0) {
            throw new IllegalStateException(
                    "BUGGY must contain nominal values YES and NO."
            );
        }

        int yesCount = 0;
        int noCount = 0;
        long missingValues = 0;

        for (Instance instance : modeling) {

            for (int attributeIndex = 0;
                 attributeIndex < modeling.numAttributes();
                 attributeIndex++) {

                if (instance.isMissing(attributeIndex)) {
                    missingValues++;
                }
            }

            if (instance.isMissing(modeling.classIndex())) {
                continue;
            }

            int classValue =
                    (int) instance.classValue();

            if (classValue == yesIndex) {
                yesCount++;
            } else if (classValue == noIndex) {
                noCount++;
            } else {
                throw new IllegalStateException(
                        "Unexpected BUGGY class value."
                );
            }
        }

        if (yesCount != EXPECTED_BUGGY_YES) {
            throw new IllegalStateException(
                    "Unexpected BUGGY=YES count: "
                            + yesCount
            );
        }

        if (noCount != EXPECTED_BUGGY_NO) {
            throw new IllegalStateException(
                    "Unexpected BUGGY=NO count: "
                            + noCount
            );
        }

        if (missingValues != 0) {
            throw new IllegalStateException(
                    "Dataset contains "
                            + missingValues
                            + " missing values."
            );
        }

        return new ValidationSummary(
                yesIndex,
                noIndex,
                yesCount,
                noCount,
                missingValues
        );
    }

    public static void main(String[] args) throws Exception {

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
                    "Usage: M2DatasetLoader [dataset.csv]"
            );
        }

        DatasetBundle dataset =
                load(datasetPath);

        Instances raw =
                dataset.raw();

        Instances modeling =
                dataset.modeling();

        System.out.println();
        System.out.println("===== M2 DATASET CHECK =====");

        System.out.println(
                "Dataset                 : "
                        + datasetPath.toAbsolutePath()
        );

        System.out.println(
                "Rows                    : "
                        + raw.numInstances()
        );

        System.out.println(
                "Original attributes     : "
                        + raw.numAttributes()
        );

        System.out.println(
                "Metadata removed        : "
                        + EXPECTED_METADATA_ATTRIBUTES
        );

        System.out.println(
                "Model attributes        : "
                        + modeling.numAttributes()
        );

        System.out.println(
                "Predictors              : "
                        + (modeling.numAttributes() - 1)
        );

        System.out.println(
                "Target                  : "
                        + modeling.classAttribute().name()
        );

        System.out.println(
                "Target type             : "
                        + (
                        modeling.classAttribute().isNominal()
                                ? "NOMINAL"
                                : "NOT NOMINAL"
                )
        );

        System.out.println(
                "BUGGY YES index         : "
                        + dataset.yesIndex()
        );

        System.out.println(
                "BUGGY NO index          : "
                        + dataset.noIndex()
        );

        System.out.println(
                "BUGGY YES               : "
                        + dataset.yesCount()
        );

        System.out.println(
                "BUGGY NO                : "
                        + dataset.noCount()
        );

        System.out.println(
                "Missing values          : "
                        + dataset.missingValues()
        );

        System.out.println(
                "NSMELLS predictor       : "
                        + (
                        modeling.attribute("NSMELLS") != null
                                ? "YES"
                                : "NO"
                )
        );

        System.out.println(
                "Identifier leakage      : NO"
        );

        System.out.println();
        System.out.println(
                "RESULT: M2 DATASET OK"
        );
    }

    public record DatasetBundle(
            Instances raw,
            Instances modeling,
            int yesIndex,
            int noIndex,
            int yesCount,
            int noCount,
            long missingValues
    ) {
    }

    private record ValidationSummary(
            int yesIndex,
            int noIndex,
            int yesCount,
            int noCount,
            long missingValues
    ) {
    }
}