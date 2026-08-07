package it.uniroma2.isw2.storm.model;

import java.time.OffsetDateTime;
import java.util.List;

public record FixCommitCandidate(
    String issueKey,
    String commitId,
    OffsetDateTime commitDate,
    int parentCount,
    boolean merge,
    boolean revert,
    boolean issueInSubject,
    int changedFiles,
    List<String> productionJavaFiles,
    String subject
) {
}