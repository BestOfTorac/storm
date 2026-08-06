package it.uniroma2.isw2.storm.model;

import java.time.Instant;

public record GitTagInfo(
    String name,
    String commitId,
    Instant commitDate
) {
}