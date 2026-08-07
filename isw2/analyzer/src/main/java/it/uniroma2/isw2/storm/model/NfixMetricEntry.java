package it.uniroma2.isw2.storm.model;

public record NfixMetricEntry(
    int releaseIndex,
    String version,
    String commitId,
    String filePath,
    int nfix
) {
}