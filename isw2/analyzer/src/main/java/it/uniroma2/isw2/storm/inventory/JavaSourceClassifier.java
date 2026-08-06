package it.uniroma2.isw2.storm.inventory;

import it.uniroma2.isw2.storm.model.SourceCategory;

import java.util.Locale;
import java.util.Objects;

public final class JavaSourceClassifier {

    private JavaSourceClassifier() {
        // Utility class.
    }

    public static SourceCategory classify(
            String filePath) {

        Objects.requireNonNull(
            filePath,
            "File path cannot be null."
        );

        String normalizedPath =
            "/"
                + filePath
                    .replace('\\', '/')
                    .toLowerCase(Locale.ROOT);

        if (isGenerated(normalizedPath)) {
            return SourceCategory.GENERATED;
        }

        if (isTest(normalizedPath)) {
            return SourceCategory.TEST;
        }

        if (isExample(normalizedPath)) {
            return SourceCategory.EXAMPLE;
        }

        if (isProduction(normalizedPath)) {
            return SourceCategory.PRODUCTION;
        }

        return SourceCategory.OTHER;
    }

    private static boolean isGenerated(
            String path) {

        return path.contains("/generated/")
            || path.contains("/generated-sources/")
            || path.contains("/gen-java/")
            || path.contains("/src/gen/")
            || path.contains("/target/generated-");
    }

    private static boolean isTest(
            String path) {

        return path.contains("/src/test/")
            || path.contains("/test/jvm/")
            || path.contains("/tests/")
            || path.startsWith("/test/")
            || path.startsWith("/tests/")
            || path.contains("/integration-test/")
            || path.contains("/storm-test-tools/");
    }

    private static boolean isExample(
            String path) {

        return path.startsWith("/examples/")
            || path.contains("/examples/")
            || path.contains("/example/")
            || path.contains("/storm-starter/");
    }

    private static boolean isProduction(
            String path) {

        return path.contains("/src/main/java/")
            || path.contains("/src/jvm/")
            || path.contains("/src/java/");
    }
}