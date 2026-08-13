package it.uniroma2.isw2.storm.m3;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import it.uniroma2.isw2.storm.m2.M2DatasetLoader;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public final class M3DatasetBuilder {

    private static final int EXPECTED_A_ROWS = 14_611;

    private static final int EXPECTED_BPLUS_ROWS = 5_338;
    private static final int EXPECTED_BPLUS_BUGGY = 610;

    private static final int EXPECTED_C_ROWS = 9_273;
    private static final int EXPECTED_C_BUGGY = 633;

    private static final int EXPECTED_A_BUGGY = 1_243;

    private static final String SMELL_ATTRIBUTE = "NSMELLS";
    private static final String CLASS_ATTRIBUTE = "BUGGY";

    private M3DatasetBuilder() {
        // Utility class.
    }

    public static BuildResult build(
            Path datasetPath,
            Path outputDirectory
    ) throws Exception {

        M2DatasetLoader.DatasetBundle bundle =
                M2DatasetLoader.load(datasetPath);

        Instances a =
                new Instances(bundle.raw());

        if (a.numInstances() != EXPECTED_A_ROWS) {
            throw new IllegalStateException(
                    "Unexpected A size: "
                            + a.numInstances()
            );
        }

        Attribute smells =
                requireAttribute(
                        a,
                        SMELL_ATTRIBUTE
                );

        Attribute buggy =
                requireAttribute(
                        a,
                        CLASS_ATTRIBUTE
                );

        if (!smells.isNumeric()) {
            throw new IllegalStateException(
                    "NSMELLS must be numeric."
            );
        }

        if (!buggy.isNominal()) {
            throw new IllegalStateException(
                    "BUGGY must be nominal."
            );
        }

        int smellIndex =
                smells.index();

        Instances bPlus =
                new Instances(
                        a,
                        0
                );

        Instances c =
                new Instances(
                        a,
                        0
                );

        bPlus.setRelationName(
                "STORM_M3_BPLUS"
        );

        c.setRelationName(
                "STORM_M3_C"
        );

        for (Instance source : a) {

            double smellValue =
                    source.value(
                            smellIndex
                    );

            if (!Double.isFinite(smellValue)
                    || smellValue < 0.0) {

                throw new IllegalStateException(
                        "Invalid NSMELLS value: "
                                + smellValue
                );
            }

            Instance copy =
                    (Instance) source.copy();

            if (smellValue > 0.0) {

                bPlus.add(copy);

            } else if (smellValue == 0.0) {

                c.add(copy);

            } else {

                throw new IllegalStateException(
                        "Unexpected NSMELLS value."
                );
            }
        }

        /*
         * B is the counterfactual copy of B+.
         *
         * Every value must remain identical to B+ except
         * NSMELLS, which is forced to zero.
         */
        Instances b =
                new Instances(
                        bPlus
                );

        b.setRelationName(
                "STORM_M3_B"
        );

        for (Instance instance : b) {

            instance.setValue(
                    smellIndex,
                    0.0
            );
        }

        validatePartitions(
                a,
                bPlus,
                b,
                c
        );

        validateCounterfactualPair(
                bPlus,
                b
        );

        Files.createDirectories(
                outputDirectory
        );

        Path bPlusPath =
                outputDirectory.resolve(
                        "storm_m3_bplus.csv"
                );

        Path bPath =
                outputDirectory.resolve(
                        "storm_m3_b.csv"
                );

        Path cPath =
                outputDirectory.resolve(
                        "storm_m3_c.csv"
                );

        saveCsv(
                bPlus,
                bPlusPath
        );

        saveCsv(
                b,
                bPath
        );

        saveCsv(
                c,
                cPath
        );

        /*
         * Reload everything from disk.
         *
         * This is important because we want to validate
         * the actual artifacts written to the repository,
         * not only the in-memory objects.
         */
        Instances persistedBPlus =
                loadCsv(
                        bPlusPath
                );

        Instances persistedB =
                loadCsv(
                        bPath
                );

        Instances persistedC =
                loadCsv(
                        cPath
                );

        validateSchema(
                a,
                persistedBPlus,
                "B+"
        );

        validateSchema(
                a,
                persistedB,
                "B"
        );

        validateSchema(
                a,
                persistedC,
                "C"
        );

        validateExactRoundTrip(
                bPlus,
                persistedBPlus,
                "B+"
        );

        validateExactRoundTrip(
                b,
                persistedB,
                "B"
        );

        validateExactRoundTrip(
                c,
                persistedC,
                "C"
        );

        validatePersistedDatasets(
                persistedBPlus,
                persistedB,
                persistedC
        );

        validateCounterfactualPair(
                persistedBPlus,
                persistedB
        );

        return new BuildResult(
                a.numInstances(),
                bPlus.numInstances(),
                b.numInstances(),
                c.numInstances(),
                countBuggyYes(bPlus),
                countBuggyYes(c),
                bPlusPath,
                bPath,
                cPath
        );
    }

    private static void validatePartitions(
            Instances a,
            Instances bPlus,
            Instances b,
            Instances c
    ) {

        if (bPlus.numInstances()
                != EXPECTED_BPLUS_ROWS) {

            throw new IllegalStateException(
                    "Unexpected B+ size: "
                            + bPlus.numInstances()
            );
        }

        if (b.numInstances()
                != EXPECTED_BPLUS_ROWS) {

            throw new IllegalStateException(
                    "Unexpected B size: "
                            + b.numInstances()
            );
        }

        if (c.numInstances()
                != EXPECTED_C_ROWS) {

            throw new IllegalStateException(
                    "Unexpected C size: "
                            + c.numInstances()
            );
        }

        if (bPlus.numInstances()
                + c.numInstances()
                != a.numInstances()) {

            throw new IllegalStateException(
                    "B+ and C do not partition A."
            );
        }

        int aBuggy =
                countBuggyYes(a);

        int bPlusBuggy =
                countBuggyYes(bPlus);

        int cBuggy =
                countBuggyYes(c);

        if (aBuggy != EXPECTED_A_BUGGY) {
            throw new IllegalStateException(
                    "Unexpected BUGGY count in A: "
                            + aBuggy
            );
        }

        if (bPlusBuggy
                != EXPECTED_BPLUS_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected BUGGY count in B+: "
                            + bPlusBuggy
            );
        }

        if (cBuggy
                != EXPECTED_C_BUGGY) {

            throw new IllegalStateException(
                    "Unexpected BUGGY count in C: "
                            + cBuggy
            );
        }

        if (bPlusBuggy
                + cBuggy
                != aBuggy) {

            throw new IllegalStateException(
                    "BUGGY partition invariant failed."
            );
        }

        validateSmells(
                bPlus,
                true,
                "B+"
        );

        validateSmells(
                b,
                false,
                "B"
        );

        validateSmells(
                c,
                false,
                "C"
        );
    }

    private static void validateExactRoundTrip(
            Instances expected,
            Instances actual,
            String name
    ) {

        if (expected.numInstances()
                != actual.numInstances()) {

            throw new IllegalStateException(
                    name
                            + " round-trip row-count mismatch."
            );
        }

        if (expected.numAttributes()
                != actual.numAttributes()) {

            throw new IllegalStateException(
                    name
                            + " round-trip attribute-count mismatch."
            );
        }

        for (int attributeIndex = 0;
             attributeIndex < expected.numAttributes();
             attributeIndex++) {

            Attribute expectedAttribute =
                    expected.attribute(
                            attributeIndex
                    );

            Attribute actualAttribute =
                    actual.attribute(
                            attributeIndex
                    );

            if (!expectedAttribute.name()
                    .equals(
                            actualAttribute.name()
                    )) {

                throw new IllegalStateException(
                        name
                                + " round-trip attribute-name mismatch at "
                                + (attributeIndex + 1)
                );
            }

            if (expectedAttribute.type()
                    != actualAttribute.type()) {

                throw new IllegalStateException(
                        name
                                + " round-trip attribute-type mismatch for "
                                + expectedAttribute.name()
                );
            }
        }

        for (int row = 0;
             row < expected.numInstances();
             row++) {

            Instance expectedInstance =
                    expected.instance(
                            row
                    );

            Instance actualInstance =
                    actual.instance(
                            row
                    );

            for (int attributeIndex = 0;
                 attributeIndex < expected.numAttributes();
                 attributeIndex++) {

                if (!sameValue(
                        expectedInstance,
                        actualInstance,
                        attributeIndex
                )) {

                    throw new IllegalStateException(
                            name
                                    + " lost information during CSV round-trip"
                                    + " at row "
                                    + (row + 1)
                                    + ", attribute "
                                    + expected.attribute(
                                            attributeIndex
                                    ).name()
                    );
                }
            }
        }
    }
    private static void validatePersistedDatasets(
            Instances bPlus,
            Instances b,
            Instances c
    ) {

        if (bPlus.numInstances()
                != EXPECTED_BPLUS_ROWS) {

            throw new IllegalStateException(
                    "Persisted B+ has wrong size."
            );
        }

        if (b.numInstances()
                != EXPECTED_BPLUS_ROWS) {

            throw new IllegalStateException(
                    "Persisted B has wrong size."
            );
        }

        if (c.numInstances()
                != EXPECTED_C_ROWS) {

            throw new IllegalStateException(
                    "Persisted C has wrong size."
            );
        }

        if (countBuggyYes(bPlus)
                != EXPECTED_BPLUS_BUGGY) {

            throw new IllegalStateException(
                    "Persisted B+ has wrong BUGGY count."
            );
        }

        if (countBuggyYes(c)
                != EXPECTED_C_BUGGY) {

            throw new IllegalStateException(
                    "Persisted C has wrong BUGGY count."
            );
        }

        validateSmells(
                bPlus,
                true,
                "Persisted B+"
        );

        validateSmells(
                b,
                false,
                "Persisted B"
        );

        validateSmells(
                c,
                false,
                "Persisted C"
        );
    }

    private static void validateSmells(
            Instances data,
            boolean mustBePositive,
            String name
    ) {

        Attribute smells =
                requireAttribute(
                        data,
                        SMELL_ATTRIBUTE
                );

        int index =
                smells.index();

        for (int row = 0;
             row < data.numInstances();
             row++) {

            double value =
                    data.instance(row)
                            .value(index);

            if (mustBePositive) {

                if (!(value > 0.0)) {

                    throw new IllegalStateException(
                            name
                                    + " contains NSMELLS <= 0 at row "
                                    + row
                    );
                }

            } else {

                if (Double.compare(
                        value,
                        0.0
                ) != 0) {

                    throw new IllegalStateException(
                            name
                                    + " contains NSMELLS != 0 at row "
                                    + row
                    );
                }
            }
        }
    }

    private static void validateCounterfactualPair(
            Instances bPlus,
            Instances b
    ) {

        if (bPlus.numInstances()
                != b.numInstances()) {

            throw new IllegalStateException(
                    "B+ and B have different row counts."
            );
        }

        if (bPlus.numAttributes()
                != b.numAttributes()) {

            throw new IllegalStateException(
                    "B+ and B have different schemas."
            );
        }

        Attribute bPlusSmells =
                requireAttribute(
                        bPlus,
                        SMELL_ATTRIBUTE
                );

        Attribute bSmells =
                requireAttribute(
                        b,
                        SMELL_ATTRIBUTE
                );

        int smellIndex =
                bPlusSmells.index();

        if (smellIndex
                != bSmells.index()) {

            throw new IllegalStateException(
                    "NSMELLS position differs between B+ and B."
            );
        }

        long changedSmellCells = 0;

        for (int row = 0;
             row < bPlus.numInstances();
             row++) {

            Instance real =
                    bPlus.instance(row);

            Instance counterfactual =
                    b.instance(row);

            for (int attributeIndex = 0;
                 attributeIndex < bPlus.numAttributes();
                 attributeIndex++) {

                if (attributeIndex
                        == smellIndex) {

                    double realValue =
                            real.value(
                                    attributeIndex
                            );

                    double counterfactualValue =
                            counterfactual.value(
                                    attributeIndex
                            );

                    if (!(realValue > 0.0)) {
                        throw new IllegalStateException(
                                "B+ NSMELLS is not positive at row "
                                        + row
                        );
                    }

                    if (Double.compare(
                            counterfactualValue,
                            0.0
                    ) != 0) {

                        throw new IllegalStateException(
                                "B NSMELLS is not zero at row "
                                        + row
                        );
                    }

                    if (Double.compare(
                            realValue,
                            counterfactualValue
                    ) != 0) {

                        changedSmellCells++;
                    }

                    continue;
                }

                if (!sameValue(
                        real,
                        counterfactual,
                        attributeIndex
                )) {

                    throw new IllegalStateException(
                            "B differs from B+ outside NSMELLS"
                                    + " at row "
                                    + row
                                    + ", attribute "
                                    + bPlus.attribute(
                                    attributeIndex
                            ).name()
                    );
                }
            }
        }

        if (changedSmellCells
                != EXPECTED_BPLUS_ROWS) {

            throw new IllegalStateException(
                    "Expected exactly "
                            + EXPECTED_BPLUS_ROWS
                            + " changed NSMELLS cells, found "
                            + changedSmellCells
            );
        }
    }

    private static boolean sameValue(
            Instance left,
            Instance right,
            int attributeIndex
    ) {

        boolean leftMissing =
                left.isMissing(
                        attributeIndex
                );

        boolean rightMissing =
                right.isMissing(
                        attributeIndex
                );

        if (leftMissing
                || rightMissing) {

            return leftMissing
                    && rightMissing;
        }

        Attribute leftAttribute =
                left.dataset()
                        .attribute(
                                attributeIndex
                        );

        Attribute rightAttribute =
                right.dataset()
                        .attribute(
                                attributeIndex
                        );

        if (leftAttribute.isNumeric()
                && rightAttribute.isNumeric()) {

            return Double.compare(
                    left.value(
                            attributeIndex
                    ),
                    right.value(
                            attributeIndex
                    )
            ) == 0;
        }

        return left.toString(
                attributeIndex
        ).equals(
                right.toString(
                        attributeIndex
                )
        );
    }

    private static void validateSchema(
            Instances reference,
            Instances candidate,
            String name
    ) {

        if (reference.numAttributes()
                != candidate.numAttributes()) {

            throw new IllegalStateException(
                    name
                            + " has unexpected attribute count: "
                            + candidate.numAttributes()
            );
        }

        for (int i = 0;
             i < reference.numAttributes();
             i++) {

            String expected =
                    reference.attribute(i)
                            .name();

            String actual =
                    candidate.attribute(i)
                            .name();

            if (!expected.equals(actual)) {

                throw new IllegalStateException(
                        name
                                + " schema mismatch at position "
                                + (i + 1)
                                + ": expected "
                                + expected
                                + ", found "
                                + actual
                );
            }
        }
    }

    private static int countBuggyYes(
            Instances data
    ) {

        Attribute buggy =
                requireAttribute(
                        data,
                        CLASS_ATTRIBUTE
                );

        int count = 0;

        for (Instance instance : data) {

            if ("YES".equals(
                    instance.stringValue(
                            buggy
                    )
            )) {

                count++;
            }
        }

        return count;
    }

    private static Attribute requireAttribute(
            Instances data,
            String name
    ) {

        Attribute attribute =
                data.attribute(name);

        if (attribute == null) {
            throw new IllegalStateException(
                    "Missing attribute: "
                            + name
            );
        }

        return attribute;
    }

    private static void saveCsv(
            Instances data,
            Path path
    ) throws Exception {

        /*
         * Do not use WEKA CSVSaver here.
         *
         * CSVSaver formats numeric attributes with limited decimal
         * precision. M3 requires B+ and C to remain numerically
         * identical to their observations in A, because even tiny
         * changes can alter RandomForest branch decisions and
         * probability estimates.
         *
         * Double.toString() guarantees a decimal representation
         * that round-trips to the exact same Java double.
         */
        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            for (int attributeIndex = 0;
                 attributeIndex < data.numAttributes();
                 attributeIndex++) {

                if (attributeIndex > 0) {
                    writer.write(',');
                }

                writer.write(
                        csv(
                                data.attribute(
                                        attributeIndex
                                ).name()
                        )
                );
            }

            writer.newLine();

            for (Instance instance : data) {

                for (int attributeIndex = 0;
                     attributeIndex < data.numAttributes();
                     attributeIndex++) {

                    if (attributeIndex > 0) {
                        writer.write(',');
                    }

                    Attribute attribute =
                            data.attribute(
                                    attributeIndex
                            );

                    String value;

                    if (instance.isMissing(
                            attributeIndex
                    )) {

                        value = "?";

                    } else if (attribute.isNumeric()) {

                        value =
                                Double.toString(
                                        instance.value(
                                                attributeIndex
                                        )
                                );

                    } else {

                        value =
                                instance.stringValue(
                                        attributeIndex
                                );
                    }

                    writer.write(
                            csv(
                                    value
                            )
                    );
                }

                writer.newLine();
            }
        }
    }

    private static String csv(
            String value
    ) {

        boolean quote =
                value.contains(",")
                        || value.contains("\"")
                        || value.contains("\n")
                        || value.contains("\r");

        if (!quote) {
            return value;
        }

        return "\""
                + value.replace(
                        "\"",
                        "\"\""
                )
                + "\"";
    }
    private static Instances loadCsv(
            Path path
    ) throws Exception {

        CSVLoader loader =
                new CSVLoader();

        loader.setSource(
                path.toFile()
        );

        return loader.getDataSet();
    }

    public static void main(
            String[] args
    ) throws Exception {

        Path datasetPath;

        Path outputDirectory;

        if (args.length == 0) {

            datasetPath =
                    Path.of(
                            "isw2",
                            "datasets",
                            "storm_m1_dataset_sonarcloud.csv"
                    );

            outputDirectory =
                    Path.of(
                            "isw2",
                            "datasets",
                            "m3"
                    );

        } else if (args.length == 2) {

            datasetPath =
                    Path.of(
                            args[0]
                    );

            outputDirectory =
                    Path.of(
                            args[1]
                    );

        } else {

            throw new IllegalArgumentException(
                    "Usage: M3DatasetBuilder "
                            + "[dataset.csv output-directory]"
            );
        }

        BuildResult result =
                build(
                        datasetPath,
                        outputDirectory
                );

        System.out.println();
        System.out.println(
                "===== M3 DATASET BUILDER ====="
        );

        System.out.println(
                "A rows                  : "
                        + result.aRows()
        );

        System.out.println(
                "B+ rows                 : "
                        + result.bPlusRows()
        );

        System.out.println(
                "B rows                  : "
                        + result.bRows()
        );

        System.out.println(
                "C rows                  : "
                        + result.cRows()
        );

        System.out.println();

        System.out.println(
                "B+ BUGGY YES            : "
                        + result.bPlusBuggyYes()
        );

        System.out.println(
                "C BUGGY YES             : "
                        + result.cBuggyYes()
        );

        System.out.println();

        System.out.println(
                "B+ NSMELLS              : > 0"
        );

        System.out.println(
                "B NSMELLS               : 0"
        );

        System.out.println(
                "C NSMELLS               : 0"
        );

        System.out.println();

        System.out.println(
                "B/B+ non-NSMELLS cells  : IDENTICAL"
        );

        System.out.println(
                "Changed B cells          : "
                        + EXPECTED_BPLUS_ROWS
                        + " NSMELLS values only"
        );

        System.out.println();

        System.out.println(
                "B+ CSV                   : "
                        + result.bPlusPath()
                                .toAbsolutePath()
        );

        System.out.println(
                "B CSV                    : "
                        + result.bPath()
                                .toAbsolutePath()
        );

        System.out.println(
                "C CSV                    : "
                        + result.cPath()
                                .toAbsolutePath()
        );

        System.out.println();
        System.out.println(
                "RESULT: M3 DATASETS OK"
        );
    }

    public record BuildResult(
            int aRows,
            int bPlusRows,
            int bRows,
            int cRows,
            int bPlusBuggyYes,
            int cBuggyYes,
            Path bPlusPath,
            Path bPath,
            Path cPath
    ) {
    }
}