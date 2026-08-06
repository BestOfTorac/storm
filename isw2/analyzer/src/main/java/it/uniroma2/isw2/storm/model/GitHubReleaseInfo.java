package it.uniroma2.isw2.storm.model;

import java.time.LocalDate;

public record GitHubReleaseInfo(
    String tagName,
    String releaseName,
    LocalDate publishedDate,
    boolean draft,
    boolean prerelease
) {
}