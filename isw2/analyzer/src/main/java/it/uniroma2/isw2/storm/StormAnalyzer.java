package it.uniroma2.isw2.storm;

import java.nio.file.Files;
import java.nio.file.Path;

public final class StormAnalyzer {

    private static final String PROJECT_NAME = "STORM";

    private StormAnalyzer() {
        // Utility class.
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                "Usage: java -jar storm-analyzer.jar <repository-path>"
            );
            System.exit(2);
        }

        Path repositoryPath = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        if (!Files.exists(repositoryPath.resolve(".git"))) {
            System.err.printf(
                "Invalid Git repository: %s%n",
                repositoryPath
            );
            System.exit(1);
        }

        System.out.printf("Project: %s%n", PROJECT_NAME);
        System.out.printf("Repository: %s%n", repositoryPath);
        System.out.println(
            "Analyzer bootstrap completed successfully."
        );
    }
}