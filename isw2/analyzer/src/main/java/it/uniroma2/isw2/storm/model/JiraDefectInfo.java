package it.uniroma2.isw2.storm.model;

import java.time.OffsetDateTime;
import java.util.List;

public record JiraDefectInfo(
    String id,
    String key,
    String issueType,
    String status,
    String resolution,
    OffsetDateTime createdAt,
    OffsetDateTime resolvedAt,
    List<String> affectedVersions,
    List<String> fixVersions,
    String summary
) {
}