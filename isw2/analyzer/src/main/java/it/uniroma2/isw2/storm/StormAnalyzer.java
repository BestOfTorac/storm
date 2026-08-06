package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.git.RepositoryInspector;
import it.uniroma2.isw2.storm.github.GitHubReleaseClient;
import it.uniroma2.isw2.storm.jira.JiraReleaseClient;
import it.uniroma2.isw2.storm.model.GitHubReleaseInfo;
import it.uniroma2.isw2.storm.model.GitTagInfo;
import it.uniroma2.isw2.storm.model.JiraReleaseInfo;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class StormAnalyzer {

    private static final String PROJECT_NAME = "STORM";
    private static final String BASELINE_TAG = "v3.0.0";

    private static final String GITHUB_OWNER = "apache";
    private static final String GITHUB_REPOSITORY = "storm";

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
                "Usage: java -jar storm-analyzer.jar "
                    + "<repository-path>"
            );
            return 2;
        }

        Path repositoryPath = Path.of(args[0])
            .toAbsolutePath()
            .normalize();

        try (
            RepositoryInspector inspector =
                RepositoryInspector.open(
                    repositoryPath
                )
        ) {
            printRepositoryInformation(
                repositoryPath,
                inspector
            );

            List<GitTagInfo> gitTags =
                inspector.readTags();

            printTagInformation(gitTags);

            JiraReleaseClient jiraClient =
                JiraReleaseClient.apacheJira();

            List<JiraReleaseInfo> jiraVersions =
                jiraClient.fetchProjectVersions(
                    PROJECT_NAME
                );

            printJiraReleaseInformation(
                jiraVersions
            );

            GitHubReleaseClient githubClient =
                GitHubReleaseClient.publicApi();

            List<GitHubReleaseInfo> githubReleases =
                githubClient.fetchRepositoryReleases(
                    GITHUB_OWNER,
                    GITHUB_REPOSITORY
                );

            printGitHubReleaseInformation(
                githubReleases,
                jiraVersions
            );

            return 0;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            System.err.println(
                "Analyzer execution interrupted."
            );

            return 1;

        } catch (
                IOException
                | GitAPIException
                | IllegalArgumentException exception) {

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

        System.out.printf(
            "Project: %s%n",
            PROJECT_NAME
        );

        System.out.printf(
            "Repository: %s%n",
            repositoryPath
        );

        System.out.printf(
            "Current branch: %s%n",
            inspector.currentBranch()
        );

        System.out.printf(
            "HEAD commit: %s%n",
            inspector.headCommitId()
        );

        Map<String, String> remotes =
            inspector.remoteUrls();

        System.out.printf(
            "Remotes found: %d%n",
            remotes.size()
        );

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

        System.out.printf(
            "Git tags found: %d%n",
            tags.size()
        );

        boolean baselineFound =
            tags.stream()
                .anyMatch(
                    tag ->
                        BASELINE_TAG.equals(
                            tag.name()
                        )
                );

        System.out.printf(
            "Baseline tag %s found: %s%n",
            BASELINE_TAG,
            baselineFound
        );

        if (tags.isEmpty()) {
            return;
        }

        System.out.println(
            "Oldest Git tags by commit date:"
        );

        tags.stream()
            .limit(PREVIEW_SIZE)
            .forEach(
                StormAnalyzer::printTag
            );

        System.out.println(
            "Newest Git tags by commit date:"
        );

        tags.stream()
            .skip(
                Math.max(
                    0,
                    tags.size() - PREVIEW_SIZE
                )
            )
            .forEach(
                StormAnalyzer::printTag
            );
    }

    private static void printJiraReleaseInformation(
            List<JiraReleaseInfo> jiraVersions) {

        List<JiraReleaseInfo> eligibleVersions =
            jiraVersions.stream()
                .filter(
                    JiraReleaseInfo::released
                )
                .filter(
                    version ->
                        !version.archived()
                )
                .toList();

        System.out.printf(
            "Jira versions with release date: %d%n",
            jiraVersions.size()
        );

        System.out.printf(
            "Released and non-archived Jira versions: %d%n",
            eligibleVersions.size()
        );

        if (eligibleVersions.isEmpty()) {
            return;
        }

        System.out.println(
            "Oldest eligible Jira releases:"
        );

        eligibleVersions.stream()
            .limit(PREVIEW_SIZE)
            .forEach(
                StormAnalyzer::printJiraRelease
            );

        System.out.println(
            "Newest eligible Jira releases:"
        );

        eligibleVersions.stream()
            .skip(
                Math.max(
                    0,
                    eligibleVersions.size()
                        - PREVIEW_SIZE
                )
            )
            .forEach(
                StormAnalyzer::printJiraRelease
            );
    }

    private static void printGitHubReleaseInformation(
            List<GitHubReleaseInfo> githubReleases,
            List<JiraReleaseInfo> jiraVersions) {

        List<GitHubReleaseInfo> stableReleases =
            githubReleases.stream()
                .filter(
                    release ->
                        !release.draft()
                )
                .filter(
                    release ->
                        !release.prerelease()
                )
                .toList();

        List<GitHubReleaseInfo> boundedReleases =
            releasesUpToBaseline(
                stableReleases
            );

        Set<String> jiraVersionNames =
            jiraVersions.stream()
                .filter(
                    JiraReleaseInfo::released
                )
                .filter(
                    version ->
                        !version.archived()
                )
                .map(
                    JiraReleaseInfo::name
                )
                .map(
                    StormAnalyzer::normalizeVersion
                )
                .collect(
                    Collectors.toSet()
                );

        List<GitHubReleaseInfo> absentFromJira =
            boundedReleases.stream()
                .filter(
                    release ->
                        !jiraVersionNames.contains(
                            normalizeVersion(
                                release.tagName()
                            )
                        )
                )
                .toList();

        System.out.printf(
            "GitHub releases returned: %d%n",
            githubReleases.size()
        );

        System.out.printf(
            "Published stable GitHub releases: %d%n",
            stableReleases.size()
        );

        System.out.printf(
            "Stable GitHub releases up to %s: %d%n",
            BASELINE_TAG,
            boundedReleases.size()
        );

        System.out.printf(
            "Stable GitHub releases absent from Jira: %d%n",
            absentFromJira.size()
        );

        if (absentFromJira.isEmpty()) {
            return;
        }

        System.out.println(
            "GitHub releases missing from Jira:"
        );

        absentFromJira.forEach(
            StormAnalyzer::printGitHubRelease
        );
    }

    private static List<GitHubReleaseInfo>
            releasesUpToBaseline(
                List<GitHubReleaseInfo> releases) {

        String normalizedBaseline =
            normalizeVersion(BASELINE_TAG);

        for (
            int index = 0;
            index < releases.size();
            index++
        ) {
            GitHubReleaseInfo release =
                releases.get(index);

            if (
                normalizedBaseline.equals(
                    normalizeVersion(
                        release.tagName()
                    )
                )
            ) {
                return List.copyOf(
                    releases.subList(
                        0,
                        index + 1
                    )
                );
            }
        }

        throw new IllegalArgumentException(
            "Baseline release not found in "
                + "GitHub Releases: "
                + BASELINE_TAG
        );
    }

    private static String normalizeVersion(
            String version) {

        if (version == null) {
            return "";
        }

        String normalized =
            version.trim()
                .toLowerCase(
                    Locale.ROOT
                );

        if (
            normalized.length() > 1
                && normalized.charAt(0) == 'v'
                && Character.isDigit(
                    normalized.charAt(1)
                )
        ) {
            return normalized.substring(1);
        }

        return normalized;
    }

    private static void printTag(
            GitTagInfo tag) {

        System.out.printf(
            "  - %-24s %s %s%n",
            tag.name(),
            shortCommitId(
                tag.commitId()
            ),
            DATE_FORMAT.format(
                tag.commitDate()
            )
        );
    }

    private static void printJiraRelease(
            JiraReleaseInfo release) {

        System.out.printf(
            "  - %-8s %-30s %s%n",
            release.id(),
            release.name(),
            release.releaseDate()
        );
    }

    private static void printGitHubRelease(
            GitHubReleaseInfo release) {

        System.out.printf(
            "  - %-24s %s%n",
            release.tagName(),
            release.publishedDate()
        );
    }

    private static String shortCommitId(
            String commitId) {

        return commitId.substring(
            0,
            Math.min(
                12,
                commitId.length()
            )
        );
    }
}