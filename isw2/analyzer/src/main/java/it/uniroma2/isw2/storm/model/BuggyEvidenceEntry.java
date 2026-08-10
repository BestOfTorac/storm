package it.uniroma2.isw2.storm.model;

public record BuggyEvidenceEntry(
    String issueKey,
    int releaseIndex,
    String version,
    String commitId,
    String filePath,
    BuggyMappingStrategy mappingStrategy,
    int effectiveIntroductionVersion,
    int fixVersion,
    String introductionVersionSource,
    String fixVersionSource,
    String blamedFilePath,
    String fixedFilePath
) {
}