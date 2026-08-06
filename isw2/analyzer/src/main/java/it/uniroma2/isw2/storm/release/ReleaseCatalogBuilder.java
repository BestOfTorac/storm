package it.uniroma2.isw2.storm.release;

import it.uniroma2.isw2.storm.model.GitHubReleaseInfo;
import it.uniroma2.isw2.storm.model.GitTagInfo;
import it.uniroma2.isw2.storm.model.JiraReleaseInfo;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ReleaseCatalogBuilder {

    private static final double DATASET_RELEASE_SHARE =
        0.34d;

    private ReleaseCatalogBuilder() {
        // Utility class.
    }

    public static List<ReleaseCatalogEntry> build(
            List<JiraReleaseInfo> jiraVersions,
            List<GitHubReleaseInfo> githubReleases,
            List<GitTagInfo> gitTags,
            String baselineTag) {

        GitHubReleaseInfo baselineRelease =
            findBaselineRelease(
                githubReleases,
                baselineTag
            );

        LocalDate baselineDate =
            baselineRelease.publishedDate();

        Map<String, JiraReleaseInfo> jiraByVersion =
            indexJiraVersions(
                jiraVersions,
                baselineDate
            );

        Map<String, GitHubReleaseInfo> githubByVersion =
            indexGitHubReleases(
                githubReleases,
                baselineDate
            );

        Map<String, GitTagInfo> tagsByVersion =
            indexGitTags(gitTags);

        Set<String> normalizedVersions =
            new LinkedHashSet<>();

        normalizedVersions.addAll(
            jiraByVersion.keySet()
        );

        normalizedVersions.addAll(
            githubByVersion.keySet()
        );

        List<ReleaseCatalogEntry> unorderedEntries =
            new ArrayList<>();

        for (String normalizedVersion
                : normalizedVersions) {

            JiraReleaseInfo jiraRelease =
                jiraByVersion.get(
                    normalizedVersion
                );

            GitHubReleaseInfo githubRelease =
                githubByVersion.get(
                    normalizedVersion
                );

            GitTagInfo gitTag =
                tagsByVersion.get(
                    normalizedVersion
                );

            LocalDate releaseDate;
            String releaseDateSource;

            if (jiraRelease != null) {
                releaseDate =
                    jiraRelease.releaseDate();

                releaseDateSource = "JIRA";

            } else {
                releaseDate =
                    githubRelease.publishedDate();

                releaseDateSource = "GITHUB";
            }

            unorderedEntries.add(
                new ReleaseCatalogEntry(
                    0,
                    normalizedVersion,
                    releaseDate,
                    releaseDateSource,
                    jiraRelease == null
                        ? ""
                        : jiraRelease.id(),
                    githubRelease == null
                        ? ""
                        : githubRelease.tagName(),
                    gitTag == null
                        ? ""
                        : gitTag.name(),
                    gitTag == null
                        ? ""
                        : gitTag.commitId(),
                    gitTag == null
                        ? null
                        : gitTag.commitDate()
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate(),
                    false
                )
            );
        }

        unorderedEntries.sort(
            Comparator
                .comparing(
                    ReleaseCatalogEntry::releaseDate
                )
                .thenComparing(
                    ReleaseCatalogEntry::version
                )
        );

        int datasetReleaseCount =
            (int) Math.ceil(
                unorderedEntries.size()
                    * DATASET_RELEASE_SHARE
            );

        List<ReleaseCatalogEntry> indexedEntries =
            new ArrayList<>();

        for (
            int index = 0;
            index < unorderedEntries.size();
            index++
        ) {
            ReleaseCatalogEntry entry =
                unorderedEntries.get(index);

            indexedEntries.add(
                new ReleaseCatalogEntry(
                    index + 1,
                    entry.version(),
                    entry.releaseDate(),
                    entry.releaseDateSource(),
                    entry.jiraVersionId(),
                    entry.githubTag(),
                    entry.gitTag(),
                    entry.gitCommitId(),
                    entry.gitCommitDate(),
                    index < datasetReleaseCount
                )
            );
        }

        return List.copyOf(indexedEntries);
    }

    public static String normalizeVersion(
            String version) {

        if (version == null) {
            return "";
        }

        String normalized =
            version.trim()
                .toLowerCase(Locale.ROOT);

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

    private static GitHubReleaseInfo
            findBaselineRelease(
                List<GitHubReleaseInfo> releases,
                String baselineTag) {

        String normalizedBaseline =
            normalizeVersion(baselineTag);

        return releases.stream()
            .filter(
                release ->
                    !release.draft()
            )
            .filter(
                release ->
                    !release.prerelease()
            )
            .filter(
                release ->
                    normalizedBaseline.equals(
                        normalizeVersion(
                            release.tagName()
                        )
                    )
            )
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Baseline release not found: "
                            + baselineTag
                    )
            );
    }

    private static Map<String, JiraReleaseInfo>
            indexJiraVersions(
                List<JiraReleaseInfo> versions,
                LocalDate baselineDate) {

        Map<String, JiraReleaseInfo> indexed =
            new LinkedHashMap<>();

        for (JiraReleaseInfo version : versions) {
            if (
                !version.released()
                    || version.archived()
                    || version.releaseDate()
                        .isAfter(baselineDate)
            ) {
                continue;
            }

            String normalizedVersion =
                normalizeVersion(
                    version.name()
                );

            indexed.merge(
                normalizedVersion,
                version,
                ReleaseCatalogBuilder
                    ::preferEarlierJiraRelease
            );
        }

        return indexed;
    }

    private static Map<String, GitHubReleaseInfo>
            indexGitHubReleases(
                List<GitHubReleaseInfo> releases,
                LocalDate baselineDate) {

        Map<String, GitHubReleaseInfo> indexed =
            new LinkedHashMap<>();

        for (
            GitHubReleaseInfo release
                : releases
        ) {
            if (
                release.draft()
                    || release.prerelease()
                    || release.publishedDate()
                        .isAfter(baselineDate)
            ) {
                continue;
            }

            String normalizedVersion =
                normalizeVersion(
                    release.tagName()
                );

            indexed.merge(
                normalizedVersion,
                release,
                ReleaseCatalogBuilder
                    ::preferEarlierGitHubRelease
            );
        }

        return indexed;
    }

    private static Map<String, GitTagInfo>
            indexGitTags(
                List<GitTagInfo> tags) {

        Map<String, GitTagInfo> indexed =
            new LinkedHashMap<>();

        for (GitTagInfo tag : tags) {
            String normalizedVersion =
                normalizeVersion(
                    tag.name()
                );

            indexed.merge(
                normalizedVersion,
                tag,
                ReleaseCatalogBuilder
                    ::preferLaterGitTag
            );
        }

        return indexed;
    }

    private static JiraReleaseInfo
            preferEarlierJiraRelease(
                JiraReleaseInfo first,
                JiraReleaseInfo second) {

        if (
            first.releaseDate()
                .isBefore(second.releaseDate())
        ) {
            return first;
        }

        return second;
    }

    private static GitHubReleaseInfo
            preferEarlierGitHubRelease(
                GitHubReleaseInfo first,
                GitHubReleaseInfo second) {

        if (
            first.publishedDate()
                .isBefore(second.publishedDate())
        ) {
            return first;
        }

        return second;
    }

    private static GitTagInfo preferLaterGitTag(
            GitTagInfo first,
            GitTagInfo second) {

        if (
            first.commitDate()
                .isAfter(second.commitDate())
        ) {
            return first;
        }

        return second;
    }
}