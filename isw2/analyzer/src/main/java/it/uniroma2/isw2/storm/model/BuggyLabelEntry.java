package it.uniroma2.isw2.storm.model;

public record BuggyLabelEntry(
    int releaseIndex,
    String version,
    String commitId,
    String filePath,
    boolean buggy
) {
}