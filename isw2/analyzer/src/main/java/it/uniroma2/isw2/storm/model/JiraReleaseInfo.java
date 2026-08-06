package it.uniroma2.isw2.storm.model;

import java.time.LocalDate;

public record JiraReleaseInfo(
    String id,
    String name,
    LocalDate releaseDate,
    boolean released,
    boolean archived
) {
}