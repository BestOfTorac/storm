package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.git.RepositoryInspector;
import it.uniroma2.isw2.storm.github.GitHubReleaseClient;
import it.uniroma2.isw2.storm.jira.JiraReleaseClient;
import it.uniroma2.isw2.storm.model.GitHubReleaseInfo;
import it.uniroma2.isw2.storm.model.GitTagInfo;
import it.uniroma2.isw2.storm.model.JiraReleaseInfo;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.release.ReleaseCatalogBuilder;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvWriter;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class ReleaseCatalogGenerator {

    private static final String JIRA_PROJECT =
        "STORM";

    private static final String GITHUB_OWNER =
        "apache";

    private static final String GITHUB_REPOSITORY =
        "storm";

    private static final String BASELINE_TAG =
        "v3.0.0";

    private ReleaseCatalogGenerator() {
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
                "Usage: ReleaseCatalogGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/release_catalog.csv"
            );

        try (
            RepositoryInspector inspector =
                RepositoryInspector.open(
                    repositoryPath
                )
        ) {
            List<GitTagInfo> gitTags =
                inspector.readTags();

            List<JiraReleaseInfo> jiraVersions =
                JiraReleaseClient.apacheJira()
                    .fetchProjectVersions(
                        JIRA_PROJECT
                    );

            List<GitHubReleaseInfo> githubReleases =
                GitHubReleaseClient.publicApi()
                    .fetchRepositoryReleases(
                        GITHUB_OWNER,
                        GITHUB_REPOSITORY
                    );

            List<ReleaseCatalogEntry> catalog =
                ReleaseCatalogBuilder.build(
                    jiraVersions,
                    githubReleases,
                    gitTags,
                    BASELINE_TAG
                );

            ReleaseCatalogCsvWriter.write(
                outputPath,
                catalog
            );

            long includedReleaseCount =
                catalog.stream()
                    .filter(
                        ReleaseCatalogEntry
                            ::includedInDataset
                    )
                    .count();

            List<ReleaseCatalogEntry> missingTags =
                catalog.stream()
                    .filter(
                        entry ->
                            entry.gitTag().isBlank()
                    )
                    .toList();

            System.out.printf(
                "Catalog entries: %d%n",
                catalog.size()
            );

            System.out.printf(
                "Dataset releases: %d%n",
                includedReleaseCount
            );

            System.out.printf(
                "Entries without matching Git tag: %d%n",
                missingTags.size()
            );

            for (ReleaseCatalogEntry entry
                    : missingTags) {

                System.out.printf(
                    "  - %s (%s)%n",
                    entry.version(),
                    entry.releaseDate()
                );
            }

            System.out.printf(
                "Catalog written to: %s%n",
                outputPath
            );

            return 0;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            System.err.println(
                "Catalog generation interrupted."
            );

            return 1;

        } catch (
                IOException
                | GitAPIException
                | IllegalArgumentException exception) {

            System.err.printf(
                "Catalog generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}