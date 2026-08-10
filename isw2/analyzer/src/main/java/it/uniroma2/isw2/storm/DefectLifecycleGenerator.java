package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.buggy.DefectLifecycleCsvWriter;
import it.uniroma2.isw2.storm.buggy.DefectLifecycleResolver;
import it.uniroma2.isw2.storm.defect.DefectCatalogCsvReader;
import it.uniroma2.isw2.storm.defect.ProportionTotalCalculator;
import it.uniroma2.isw2.storm.git.NativeReleaseContainmentReader;
import it.uniroma2.isw2.storm.model.DefectLifecycle;
import it.uniroma2.isw2.storm.model.JiraDefectInfo;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;
import it.uniroma2.isw2.storm.szz.SzzCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class DefectLifecycleGenerator {

    private DefectLifecycleGenerator() {
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

        if (args.length != 1) {

            System.err.println(
                "Usage: DefectLifecycleGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path releaseCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/release_catalog.csv"
            );

        Path defectCatalogPath =
            repositoryPath.resolve(
                "isw2/datasets/defect_catalog.csv"
            );

        Path szzPath =
            repositoryPath.resolve(
                "isw2/datasets/"
                    + "szz_bug_introducing_changes.csv"
            );

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/"
                    + "defect_lifecycle.csv"
            );

        try {
            List<ReleaseCatalogEntry> releases =
                ReleaseCatalogCsvReader.read(
                    releaseCatalogPath
                );

            List<JiraDefectInfo> defects =
                DefectCatalogCsvReader.read(
                    defectCatalogPath
                );

            List<SzzBugIntroducingChange> szzRows =
                SzzCsvReader.read(
                    szzPath
                );

            ProportionTotalCalculator.Result
                proportionResult =
                    ProportionTotalCalculator
                        .calculate(
                            releases,
                            defects
                        );

            NativeReleaseContainmentReader
                containmentReader =
                    new NativeReleaseContainmentReader(
                        repositoryPath
                    );

            DefectLifecycleResolver.Result
                lifecycleResult =
                    DefectLifecycleResolver.resolve(
                        releases,
                        defects,
                        szzRows,
                        proportionResult.pTotal(),
                        containmentReader
                    );

            DefectLifecycleCsvWriter.write(
                outputPath,
                lifecycleResult.lifecycles()
            );

            long overlapping =
                lifecycleResult
                    .lifecycles()
                    .stream()
                    .filter(
                        DefectLifecycle
                            ::overlapsDataset
                    )
                    .count();

            int retainedSzzRows =
                lifecycleResult
                    .lifecycles()
                    .stream()
                    .mapToInt(
                        DefectLifecycle::szzRows
                    )
                    .sum();

            int retainedSzzFixes =
                lifecycleResult
                    .lifecycles()
                    .stream()
                    .mapToInt(
                        DefectLifecycle
                            ::szzFixCommits
                    )
                    .sum();

            System.out.printf(
                "Official releases: %d%n",
                releases.size()
            );

            System.out.printf(
                "Eligible Jira defects: %d%n",
                defects.size()
            );

            System.out.printf(
                "SZZ input rows: %d%n",
                szzRows.size()
            );

            System.out.printf(
                "SZZ defects: %d%n",
                lifecycleResult
                    .lifecycles()
                    .size()
            );

            System.out.printf(
                Locale.ROOT,
                "P_TOTAL: %.15f%n",
                proportionResult.pTotal()
            );

            System.out.printf(
                "P_TOTAL source defects: %d%n",
                proportionResult
                    .usableDefects()
            );

            System.out.printf(
                "FV from Jira: %d%n",
                lifecycleResult
                    .jiraFixVersions()
            );

            System.out.printf(
                "FV from Git containment: %d%n",
                lifecycleResult
                    .gitContainmentFixVersions()
            );

            System.out.printf(
                "IV from Jira affected version: %d%n",
                lifecycleResult
                    .jiraIntroductionVersions()
            );

            System.out.printf(
                "IV from Proportion Total: %d%n",
                lifecycleResult
                    .proportionIntroductionVersions()
            );

            System.out.printf(
                "Lifecycle overlapping dataset: %d%n",
                overlapping
            );

            System.out.printf(
                "SZZ rows retained: %d%n",
                retainedSzzRows
            );

            System.out.printf(
                "SZZ fix references retained: %d%n",
                retainedSzzFixes
            );

            System.out.printf(
                "Git ancestry fallback reads: %d%n",
                containmentReader
                    .ancestryReadCount()
            );

            System.out.printf(
                "Lifecycle output written to: %s%n",
                outputPath
            );

            return 0;

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "Defect lifecycle generation interrupted."
            );

            return 1;

        } catch (
                IOException
                | IllegalArgumentException
                | IllegalStateException exception) {

            System.err.printf(
                "Defect lifecycle generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}