package it.uniroma2.isw2.storm.model;

public record LocMetricEntry(
    int releaseIndex,
    String version,
    String commitId,
    String filePath,
    int loc
) {
}