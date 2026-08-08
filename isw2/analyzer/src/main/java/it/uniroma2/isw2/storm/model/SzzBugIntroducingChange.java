package it.uniroma2.isw2.storm.model;

public record SzzBugIntroducingChange(
    String issueKey,
    String fixCommitId,
    FixCommitSelectionStrategy fixSelectionStrategy,
    String parentCommitId,
    String fixedFilePath,
    String blamedFilePath,
    String bugIntroducingCommitId,
    int blamedLineCount
) {
}