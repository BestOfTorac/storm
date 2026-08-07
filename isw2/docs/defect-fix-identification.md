# Defect and fix commit identification

## Scope

This document describes how Jira defects and their corresponding Git
fix commits are identified for the ISW2 Apache Storm dataset.

The resulting artifacts are:

- `isw2/datasets/defect_catalog.csv`
- `isw2/datasets/fix_commit_candidates.csv`
- `isw2/datasets/fix_commit_catalog.csv`

These artifacts are used as the basis for the `NFIX` metric and,
subsequently, for SZZ-based bug labeling.

## Jira defect selection

The Jira project is `STORM`.

A ticket is eligible when it satisfies:

- issue type: `Bug`, corresponding to the defect type required by the
  assignment;
- status: `Closed` or `Resolved`;
- resolution: `Fixed`.

The Jira catalog contains:

- 1193 eligible defects;
- 745 defects with at least one affected version;
- 1146 defects with at least one fix version;
- 730 defects with both;
- 32 defects without version metadata.

Tickets without affected or fix versions are retained. Missing Jira
version information is not inferred during defect extraction.

## Git history considered

Apache Storm contains parallel maintenance histories.

Scanning only the history reachable from `v3.0.0` is insufficient:

- commits reachable from `v3.0.0`: 11798;
- commits reachable from all Git tags: 15333;
- additional tagged-history commits: 3535.

Therefore fix-commit discovery uses the union of the histories reachable
from the 52 official release commits recorded in
`release_catalog.csv`.

This avoids depending on arbitrary Git tags while preserving maintenance
branches associated with official releases.

## Exact Jira-key matching

Commit messages are scanned using exact Jira-key tokens matching:

`STORM-\d+`

Matching is case-insensitive.

A key such as `STORM-75` must not match `STORM-750`, `STORM-753`,
`STORM-756`, and similar issue identifiers.

Only Jira keys belonging to the eligible defect catalog are retained.

## Fix commit candidates

For each matching commit the analyzer records:

- Jira issue key;
- Git commit identifier;
- commit date;
- parent count;
- whether the commit is a merge;
- whether the commit is a revert;
- whether the issue key appears in the subject;
- changed-file count;
- changed production Java files;
- commit subject.

Candidate discovery produced:

- 4283 issue-commit pairs;
- 1101 Jira defects with an exact Git reference;
- 92 Jira defects without an exact Git reference;
- 4196 unique referenced commits;
- 1934 candidate pairs touching production Java;
- 1076 non-merge production candidates;
- 858 merge production candidates;
- 10 revert candidates.

## Production Java scope

Only production Java files contribute to class-level `NFIX`.

Commits affecting only tests, documentation, changelogs, configuration,
or non-Java files do not increment `NFIX` for production Java classes.

## Reverts

Commits explicitly identified as reverts are excluded from ordinary
fix-commit selection.

They represent the reversal of previous changes and are not counted as
additional defect fixes.

## Fix commit selection

Candidate commits are grouped by Jira defect.

### Primary strategy: non-merge commits

If a defect has at least one non-revert, non-merge commit that changes
production Java code, all such implementation commits are selected.

Merge commits belonging to the same defect are not selected, preventing
the same implementation from being counted again at integration time.

### Fallback strategy: official first-parent merge

If a defect has no usable non-merge production commit, a merge commit
may be selected only if:

1. it changes production Java code;
2. it is not a revert;
3. it belongs to the first-parent history of at least one official
   release commit.

The union of official first-parent histories contains 7142 commits.

This criterion rejects branch-synchronization merges such as merges of
`master`, `upstream/master`, or maintenance branches into a feature
branch when those commits never become first-parent integration commits
of an official release line.

Before this fallback:

- 682 defects had a production Java candidate;
- 584 had at least one non-merge production candidate;
- 98 were merge-only.

Among the 98 merge-only defects:

- 68 had at least one official first-parent merge;
- 30 had no reliable official first-parent merge and were rejected.

## Final fix catalog

The final catalog contains:

- 652 selected Jira defects;
- 1159 selected issue-commit pairs;
- 1146 unique selected Git commits;
- 584 defects selected through non-merge commits;
- 68 defects selected through first-parent merge fallback;
- 1074 `NON_MERGE` rows;
- 85 `FIRST_PARENT_MERGE` rows.

Validation found:

- 0 duplicate `(IssueKey, CommitId)` pairs;
- 0 invalid `NON_MERGE` rows;
- 0 invalid `FIRST_PARENT_MERGE` rows;
- 0 selected rows without production Java files.

## NFIX semantics

The assignment defines `NFIX` as the number of defect fixes.

Therefore `NFIX` is counted by distinct Jira defect, not by the number
of commits used to implement that defect.

For example, if three commits associated with the same Jira defect all
modify the same class, that defect contributes exactly one unit to
`NFIX` for that class.

If the same defect modifies multiple production classes, it contributes
one unit to each affected class.

Like the other historical metrics marked with `*`, `NFIX` is computed
cumulatively from release 0 for each release history.

## Separation from bugginess labeling

`NFIX` and the final `BUGGY` label are related but distinct.

`NFIX` measures prior defect fixes affecting a class.

The `BUGGY` label will be produced later using the assignment labeling
procedure based on Jira version information, SZZ, and Proportion Total.

Tickets that cannot be reliably mapped to a Git production fix are not
silently removed from the Jira defect catalog; they remain available
for the subsequent labeling phase.