package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.defect.DefectIssueKeyReader;
import it.uniroma2.isw2.storm.defect.FixCommitCandidateCsvReader;
import it.uniroma2.isw2.storm.defect.FixCommitCatalogCsvWriter;
import it.uniroma2.isw2.storm.defect.FixCommitSelector;
import it.uniroma2.isw2.storm.git.OfficialReleaseFirstParentReader;
import it.uniroma2.isw2.storm.model.FixCommitCandidate;
import it.uniroma2.isw2.storm.model.FixCommitSelectionStrategy;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.model.SelectedFixCommit;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class FixCommitCatalogGenerator {

    private FixCommitCatalogGenerator() {
        // Application entry point.
    }

    public static void main(String[] args) {

        int exitCode =
            run(args);

        if (exitCode != 0) {
            System.exit(
                exitCode
            );
        }
    }

    private static int run(
            String[] args) {

        if (args.length != 1) {
            System.err.println(
                "Usage: FixCommitCatalogGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path defectCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/defect_catalog.csv"
            );

        Path candidatePath =
            repositoryPath.resolve(
                "isw2/datasets/fix_commit_candidates.csv"
            );

        Path releaseCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/release_catalog.csv"
            );

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/fix_commit_catalog.csv"
            );

        try {
            Set<String> eligibleDefects =
                DefectIssueKeyReader.read(
                    defectCatalogPath
                );

            List<FixCommitCandidate> candidates =
                FixCommitCandidateCsvReader.read(
                    candidatePath
                );

            List<ReleaseCatalogEntry> releases =
                ReleaseCatalogCsvReader.read(
                    releaseCatalogPath
                );

            List<String> releaseCommits =
                releases.stream()
                    .map(
                        ReleaseCatalogEntry::gitCommitId
                    )
                    .filter(
                        commit ->
                            commit != null
                                && !commit.isBlank()
                    )
                    .distinct()
                    .toList();

            Set<String> firstParentCommits =
                new OfficialReleaseFirstParentReader(
                    repositoryPath
                )
                .read(
                    releaseCommits
                );

            List<SelectedFixCommit> fixes =
                FixCommitSelector.select(
                    candidates,
                    firstParentCommits
                );

            FixCommitCatalogCsvWriter.write(
                outputPath,
                fixes
            );

            Set<String> productionCandidateDefects =
                candidates.stream()
                    .filter(
                        candidate ->
                            !candidate.revert()
                                && !candidate
                                    .productionJavaFiles()
                                    .isEmpty()
                    )
                    .map(
                        FixCommitCandidate::issueKey
                    )
                    .collect(
                        Collectors.toSet()
                    );

            Set<String> selectedDefects =
                fixes.stream()
                    .map(
                        SelectedFixCommit::issueKey
                    )
                    .collect(
                        Collectors.toSet()
                    );

            long nonMergeDefects =
                fixes.stream()
                    .filter(
                        fix ->
                            fix.selectionStrategy()
                                == FixCommitSelectionStrategy
                                    .NON_MERGE
                    )
                    .map(
                        SelectedFixCommit::issueKey
                    )
                    .distinct()
                    .count();

            long fallbackDefects =
                fixes.stream()
                    .filter(
                        fix ->
                            fix.selectionStrategy()
                                == FixCommitSelectionStrategy
                                    .FIRST_PARENT_MERGE
                    )
                    .map(
                        SelectedFixCommit::issueKey
                    )
                    .distinct()
                    .count();

            long uniqueFixCommits =
                fixes.stream()
                    .map(
                        SelectedFixCommit::commitId
                    )
                    .distinct()
                    .count();

            System.out.printf(
                "Eligible Jira defects: %d%n",
                eligibleDefects.size()
            );

            System.out.printf(
                "Production candidate defects: %d%n",
                productionCandidateDefects.size()
            );

            System.out.printf(
                "Selected fix defects: %d%n",
                selectedDefects.size()
            );

            System.out.printf(
                "Non-merge fix defects: %d%n",
                nonMergeDefects
            );

            System.out.printf(
                "First-parent merge fallback defects: %d%n",
                fallbackDefects
            );

            System.out.printf(
                "Rejected merge-only defects: %d%n",
                productionCandidateDefects.size()
                    - selectedDefects.size()
            );

            System.out.printf(
                "Selected issue-commit pairs: %d%n",
                fixes.size()
            );

            System.out.printf(
                "Unique selected fix commits: %d%n",
                uniqueFixCommits
            );

            System.out.printf(
                "Official first-parent commits: %d%n",
                firstParentCommits.size()
            );

            System.out.printf(
                "Fix commit catalog written to: %s%n",
                outputPath
            );

            return 0;

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "Fix commit catalog generation interrupted."
            );

            return 1;

        } catch (
                IOException
                | IllegalArgumentException exception) {

            System.err.printf(
                "Fix commit catalog generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}