package it.uniroma2.isw2.storm.model;

public record DefectLifecycle(
    String issueKey,
    Integer observedIntroductionVersion,
    String observedIntroductionVersionName,
    int openingVersion,
    String openingVersionName,
    int fixVersion,
    String fixVersionName,
    FixVersionSource fixVersionSource,
    IntroductionVersionSource introductionVersionSource,
    Double estimatedLav,
    int effectiveIntroductionVersion,
    Double proportionUsed,
    boolean overlapsDataset,
    int szzFixCommits,
    int szzRows,
    String jiraAffectedVersions,
    String jiraFixVersions
) {

    public enum FixVersionSource {
        JIRA_FIX_VERSION,
        GIT_CONTAINMENT
    }

    public enum IntroductionVersionSource {
        JIRA_AFFECTED_VERSION,
        PROPORTION_TOTAL
    }
}