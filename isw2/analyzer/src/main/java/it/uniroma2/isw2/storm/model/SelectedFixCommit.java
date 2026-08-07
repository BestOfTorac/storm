package it.uniroma2.isw2.storm.model;

import java.time.OffsetDateTime;
import java.util.List;

public record SelectedFixCommit(
    String issueKey,
    String commitId,
    OffsetDateTime commitDate,
    FixCommitSelectionStrategy selectionStrategy,
    int parentCount,
    boolean merge,
    boolean issueInSubject,
    List<String> productionJavaFiles,
    String subject
) {
    public SelectedFixCommit {
        productionJavaFiles =
            List.copyOf(productionJavaFiles);
    }
}