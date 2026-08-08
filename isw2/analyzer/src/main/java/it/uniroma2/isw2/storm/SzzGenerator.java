package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.defect.FixCommitCatalogCsvReader;
import it.uniroma2.isw2.storm.git.NativeSzzReader;
import it.uniroma2.isw2.storm.model.FixCommitSelectionStrategy;
import it.uniroma2.isw2.storm.model.SelectedFixCommit;
import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;
import it.uniroma2.isw2.storm.szz.SzzCsvWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SzzGenerator {

    private SzzGenerator() {
        // Application entry point.
    }

    public static void main(String[] args) {

        int exitCode =
            run(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(
            String[] args) {

        if (
            args.length < 1
                || args.length > 2
        ) {
            System.err.println(
                "Usage: SzzGenerator "
                    + "<repository-path> [issue-key]"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        String issueFilter =
            args.length == 2
                ? args[1]
                    .trim()
                    .toUpperCase(Locale.ROOT)
                : null;

        Path fixCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/fix_commit_catalog.csv"
            );

        Path outputPath =
            issueFilter == null
                ? repositoryPath.resolve(
                    "isw2/datasets/"
                        + "szz_bug_introducing_changes.csv"
                )
                : repositoryPath.resolve(
                    "isw2/results/szz_"
                        + issueFilter
                        + ".csv"
                );

        try {
            List<SelectedFixCommit> allFixes =
                FixCommitCatalogCsvReader.read(
                    fixCatalogPath
                );

            /*
             * Basic SZZ is deliberately applied only to non-merge
             * bug-fixing commits.
             *
             * A merge has multiple parents and its first-parent diff
             * can include unrelated branch synchronization changes.
             * Such commits remain valid for cumulative NFIX, where
             * only fix reachability is needed, but are excluded from
             * causal line-level blame.
             */
            List<SelectedFixCommit> selectedFixes =
                allFixes.stream()
                    .filter(
                        fix ->
                            fix.selectionStrategy()
                                == FixCommitSelectionStrategy.NON_MERGE
                    )
                    .filter(
                        fix ->
                            issueFilter == null
                                || fix.issueKey()
                                    .equalsIgnoreCase(
                                        issueFilter
                                    )
                    )
                    .toList();

            if (selectedFixes.isEmpty()) {
                throw new IOException(
                    "No selected fix commits found"
                        + (
                            issueFilter == null
                                ? "."
                                : " for "
                                    + issueFilter
                                    + "."
                        )
                );
            }

            NativeSzzReader reader =
                new NativeSzzReader(
                    repositoryPath,
                    allFixes
                );

            List<SzzBugIntroducingChange> output =
                new ArrayList<>();

            for (SelectedFixCommit fix
                    : selectedFixes) {

                output.addAll(
                    reader.analyze(fix)
                );
            }

            output.sort(
                Comparator
                    .comparing(
                        SzzBugIntroducingChange
                            ::issueKey
                    )
                    .thenComparing(
                        SzzBugIntroducingChange
                            ::fixCommitId
                    )
                    .thenComparing(
                        SzzBugIntroducingChange
                            ::fixedFilePath
                    )
                    .thenComparing(
                        SzzBugIntroducingChange
                            ::bugIntroducingCommitId
                    )
            );

            SzzCsvWriter.write(
                outputPath,
                output
            );

            long issuesWithEvidence =
                output.stream()
                    .map(
                        SzzBugIntroducingChange
                            ::issueKey
                    )
                    .distinct()
                    .count();

            long uniqueBicCommits =
                output.stream()
                    .map(
                        SzzBugIntroducingChange
                            ::bugIntroducingCommitId
                    )
                    .distinct()
                    .count();

            System.out.printf(
                "Input issue-commit pairs: %d%n",
                selectedFixes.size()
            );

            System.out.printf(
                "Input defects: %d%n",
                selectedFixes.stream()
                    .map(
                        SelectedFixCommit::issueKey
                    )
                    .distinct()
                    .count()
            );

            System.out.printf(
                "Unique fix commits analyzed: %d%n",
                reader.analyzedFixCommitCount()
            );

            System.out.printf(
                "SZZ rows: %d%n",
                output.size()
            );

            System.out.printf(
                "Defects with SZZ evidence: %d%n",
                issuesWithEvidence
            );

            System.out.printf(
                "Unique bug-introducing commits: %d%n",
                uniqueBicCommits
            );

            System.out.printf(
                "Git diff reads: %d%n",
                reader.diffReadCount()
            );

            System.out.printf(
                "Git blame range reads: %d%n",
                reader.blameReadCount()
            );

            System.out.printf(
                "Fix commits without deleted "
                    + "production lines: %d%n",
                reader
                    .fixCommitsWithoutDeletedProductionLinesCount()
            );

            System.out.printf(
                "Production files with deleted lines: %d%n",
                reader
                    .productionFilesWithDeletedLinesCount()
            );

            System.out.printf(
                "Same-issue fix blames skipped: %d%n",
                reader
                    .sameIssueFixBlamesSkippedCount()
            );

            System.out.printf(
                "SZZ output written to: %s%n",
                outputPath
            );

            return 0;

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "SZZ generation interrupted."
            );

            return 1;

        } catch (
                IOException
                | IllegalArgumentException exception) {

            System.err.printf(
                "SZZ generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}