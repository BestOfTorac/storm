package it.uniroma2.isw2.storm.model;

public record SonarSmellMetric(
    int releaseIndex,
    String version,
    String commitId,
    String filePath,
    int nSmells,
    String analysisStatus
) {
}