package it.uniroma2.isw2.storm.defect;

import it.uniroma2.isw2.storm.model.FixCommitCandidate;
import it.uniroma2.isw2.storm.model.FixCommitSelectionStrategy;
import it.uniroma2.isw2.storm.model.SelectedFixCommit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class FixCommitSelector {

    private FixCommitSelector() {
        // Utility class.
    }

    public static List<SelectedFixCommit> select(
            List<FixCommitCandidate> candidates,
            Set<String> officialFirstParentCommits) {

        Objects.requireNonNull(
            candidates,
            "Candidates cannot be null."
        );

        Objects.requireNonNull(
            officialFirstParentCommits,
            "First-parent commits cannot be null."
        );

        Map<String, List<FixCommitCandidate>> byIssue =
            candidates.stream()
                .filter(
                    candidate ->
                        !candidate.revert()
                            && !candidate
                                .productionJavaFiles()
                                .isEmpty()
                )
                .collect(
                    Collectors.groupingBy(
                        FixCommitCandidate::issueKey
                    )
                );

        List<SelectedFixCommit> selected =
            new ArrayList<>();

        for (
            Map.Entry<
                String,
                List<FixCommitCandidate>
            > entry
                : byIssue.entrySet()
        ) {
            List<FixCommitCandidate> issueCandidates =
                entry.getValue();

            List<FixCommitCandidate> nonMerge =
                issueCandidates.stream()
                    .filter(
                        candidate ->
                            !candidate.merge()
                    )
                    .toList();

            if (!nonMerge.isEmpty()) {

                for (
                    FixCommitCandidate candidate
                        : nonMerge
                ) {
                    selected.add(
                        toSelected(
                            candidate,
                            FixCommitSelectionStrategy
                                .NON_MERGE
                        )
                    );
                }

                continue;
            }

            for (
                FixCommitCandidate candidate
                    : issueCandidates
            ) {
                if (
                    candidate.merge()
                        && officialFirstParentCommits
                            .contains(
                                candidate.commitId()
                            )
                ) {
                    selected.add(
                        toSelected(
                            candidate,
                            FixCommitSelectionStrategy
                                .FIRST_PARENT_MERGE
                        )
                    );
                }
            }
        }

        selected.sort(
            Comparator
                .comparingInt(
                    (SelectedFixCommit fix) ->
                        issueNumber(
                            fix.issueKey()
                        )
                )
                .thenComparing(
                    SelectedFixCommit::commitDate
                )
                .thenComparing(
                    SelectedFixCommit::commitId
                )
        );

        return List.copyOf(
            selected
        );
    }

    private static SelectedFixCommit toSelected(
            FixCommitCandidate candidate,
            FixCommitSelectionStrategy strategy) {

        return new SelectedFixCommit(
            candidate.issueKey(),
            candidate.commitId(),
            candidate.commitDate(),
            strategy,
            candidate.parentCount(),
            candidate.merge(),
            candidate.issueInSubject(),
            candidate.productionJavaFiles(),
            candidate.subject()
        );
    }

    private static int issueNumber(
            String issueKey) {

        int separator =
            issueKey.lastIndexOf('-');

        return Integer.parseInt(
            issueKey.substring(
                separator + 1
            )
        );
    }
}