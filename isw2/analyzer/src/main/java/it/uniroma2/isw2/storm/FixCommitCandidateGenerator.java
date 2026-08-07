package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.defect.DefectIssueKeyReader;
import it.uniroma2.isw2.storm.defect.FixCommitCandidateCsvWriter;
import it.uniroma2.isw2.storm.git.GitFixCommitCandidateReader;
import it.uniroma2.isw2.storm.model.FixCommitCandidate;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class FixCommitCandidateGenerator {

    private FixCommitCandidateGenerator() {
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
                "Usage: FixCommitCandidateGenerator "
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

        Path releaseCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/release_catalog.csv"
            );

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/fix_commit_candidates.csv"
            );

        try {
            Set<String> eligibleIssueKeys =
                DefectIssueKeyReader.read(
                    defectCatalogPath
                );

            List<ReleaseCatalogEntry> releaseCatalog =
                ReleaseCatalogCsvReader.read(
                    releaseCatalogPath
                );

            List<String> releaseCommits =
                releaseCatalog.stream()
                    .map(
                        ReleaseCatalogEntry::gitCommitId
                    )
                    .filter(
                        commitId ->
                            commitId != null
                                && !commitId.isBlank()
                    )
                    .distinct()
                    .toList();

            if (releaseCommits.isEmpty()) {
                throw new IOException(
                    "No Git release commits found "
                        + "in release catalog."
                );
            }

            GitFixCommitCandidateReader reader =
                new GitFixCommitCandidateReader(
                    repositoryPath
                );

            List<FixCommitCandidate> candidates =
                reader.read(
                    releaseCommits,
                    eligibleIssueKeys
                );

            FixCommitCandidateCsvWriter.write(
                outputPath,
                candidates
            );

            Set<String> defectsWithReferences =
                candidates.stream()
                    .map(
                        FixCommitCandidate::issueKey
                    )
                    .collect(
                        Collectors.toSet()
                    );

            long uniqueCommits =
                candidates.stream()
                    .map(
                        FixCommitCandidate::commitId
                    )
                    .distinct()
                    .count();

            long productionCandidates =
                candidates.stream()
                    .filter(
                        candidate ->
                            !candidate
                                .productionJavaFiles()
                                .isEmpty()
                    )
                    .count();

            long nonMergeProductionCandidates =
                candidates.stream()
                    .filter(
                        candidate ->
                            !candidate.merge()
                                && !candidate
                                    .productionJavaFiles()
                                    .isEmpty()
                    )
                    .count();

            long mergeProductionCandidates =
                candidates.stream()
                    .filter(
                        candidate ->
                            candidate.merge()
                                && !candidate
                                    .productionJavaFiles()
                                    .isEmpty()
                    )
                    .count();

            long revertCandidates =
                candidates.stream()
                    .filter(
                        FixCommitCandidate::revert
                    )
                    .count();

            System.out.printf(
                "Official release roots scanned: %d%n",
                releaseCommits.size()
            );

            System.out.printf(
                "Eligible Jira defects: %d%n",
                eligibleIssueKeys.size()
            );

            System.out.printf(
                "Candidate issue-commit pairs: %d%n",
                candidates.size()
            );

            System.out.printf(
                "Unique referenced defects: %d%n",
                defectsWithReferences.size()
            );

            System.out.printf(
                "Defects without exact Git reference: %d%n",
                eligibleIssueKeys.size()
                    - defectsWithReferences.size()
            );

            System.out.printf(
                "Unique referenced commits: %d%n",
                uniqueCommits
            );

            System.out.printf(
                "Candidates touching production Java: %d%n",
                productionCandidates
            );

            System.out.printf(
                "Non-merge production candidates: %d%n",
                nonMergeProductionCandidates
            );

            System.out.printf(
                "Merge production candidates: %d%n",
                mergeProductionCandidates
            );

            System.out.printf(
                "Revert candidates: %d%n",
                revertCandidates
            );

            System.out.printf(
                "Candidate catalog written to: %s%n",
                outputPath
            );

            return 0;

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "Fix commit candidate generation interrupted."
            );

            return 1;

        } catch (
                IOException
                | IllegalArgumentException exception) {

            System.err.printf(
                "Fix commit candidate generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}