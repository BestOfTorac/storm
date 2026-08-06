package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.git.RepositoryInspector;
import it.uniroma2.isw2.storm.model.GitTagInfo;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class StormAnalyzer {

    private static final String PROJECT_NAME = "STORM";
    private static final String BASELINE_TAG = "v3.0.0";
    private static final int PREVIEW_SIZE = 5;

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ISO_LOCAL_DATE
            .withZone(ZoneOffset.UTC);

    private StormAnalyzer() {
        // Application entry point.
    }

    public static void main(String[] args) {
        int exitCode = run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        if (args.length != 1) {
            System.err.println(
                "Usage: java -jar storm-analyzer.jar <repository-path>"
            );
            return 2;
        }

        Path repositoryPath = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        try (
            RepositoryInspector inspector =
                RepositoryInspector.open(repositoryPath)
        ) {
            printRepositoryInformation(
                repositoryPath,
                inspector
            );

            List<GitTagInfo> tags = inspector.readTags();

            printTagInformation(tags);

            return 0;

        } catch (IOException | GitAPIException exception) {
            System.err.printf(
                "Analyzer execution failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }

    private static void printRepositoryInformation(
            Path repositoryPath,
            RepositoryInspector inspector)
            throws IOException {

        System.out.printf("Project: %s%n", PROJECT_NAME);
        System.out.printf("Repository: %s%n", repositoryPath);
        System.out.printf(
            "Current branch: %s%n",
            inspector.currentBranch()
        );
        System.out.printf(
            "HEAD commit: %s%n",
            inspector.headCommitId()
        );

        Map<String, String> remotes = inspector.remoteUrls();

        System.out.printf("Remotes found: %d%n", remotes.size());

        remotes.forEach(
            (name, url) ->
                System.out.printf(
                    "  - %s: %s%n",
                    name,
                    url
                )
        );
    }

    private static void printTagInformation(
            List<GitTagInfo> tags) {

        System.out.printf("Tags found: %d%n", tags.size());

        boolean baselineFound = tags.stream()
            .anyMatch(tag -> BASELINE_TAG.equals(tag.name()));

        System.out.printf(
            "Baseline tag %s found: %s%n",
            BASELINE_TAG,
            baselineFound
        );

        if (tags.isEmpty()) {
            return;
        }

        System.out.println("Oldest tags by commit date:");

        tags.stream()
            .limit(PREVIEW_SIZE)
            .forEach(StormAnalyzer::printTag);

        System.out.println("Newest tags by commit date:");

        tags.stream()
            .skip(Math.max(0, tags.size() - PREVIEW_SIZE))
            .forEach(StormAnalyzer::printTag);
    }

    private static void printTag(GitTagInfo tag) {
        System.out.printf(
            "  - %-24s %s %s%n",
            tag.name(),
            shortCommitId(tag.commitId()),
            DATE_FORMAT.format(tag.commitDate())
        );
    }

    private static String shortCommitId(String commitId) {
        return commitId.substring(
            0,
            Math.min(12, commitId.length())
        );
    }
}