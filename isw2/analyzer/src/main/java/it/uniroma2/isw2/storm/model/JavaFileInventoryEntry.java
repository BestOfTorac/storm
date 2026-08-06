package it.uniroma2.isw2.storm.model;

public record JavaFileInventoryEntry(
    int releaseIndex,
    String version,
    String commitId,
    String filePath,
    SourceCategory sourceCategory
) {
}