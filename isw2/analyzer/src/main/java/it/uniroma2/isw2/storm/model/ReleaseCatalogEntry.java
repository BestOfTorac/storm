package it.uniroma2.isw2.storm.model;

import java.time.LocalDate;

public record ReleaseCatalogEntry(
    int index,
    String version,
    LocalDate releaseDate,
    String releaseDateSource,
    String jiraVersionId,
    String githubTag,
    String gitTag,
    String gitCommitId,
    LocalDate gitCommitDate,
    boolean includedInDataset
) {
}