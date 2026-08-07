package it.uniroma2.isw2.storm.model;

public record HistoricalMetricEntry(
    int releaseIndex,
    String version,
    String commitId,
    String filePath,
    HistoricalMetrics metrics
) {
}