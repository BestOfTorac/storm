package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.buggy.BuggyEvidenceCsvWriter;
import it.uniroma2.isw2.storm.buggy.BuggyLabelResolver;
import it.uniroma2.isw2.storm.buggy.BuggyLabelsCsvWriter;
import it.uniroma2.isw2.storm.buggy.DefectLifecycleCsvReader;
import it.uniroma2.isw2.storm.git.NativeClassLineageReader;
import it.uniroma2.isw2.storm.inventory.JavaInventoryCsvReader;
import it.uniroma2.isw2.storm.model.BuggyLabelEntry;
import it.uniroma2.isw2.storm.model.DefectLifecycle;
import it.uniroma2.isw2.storm.model.JavaFileInventoryEntry;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;
import it.uniroma2.isw2.storm.szz.SzzCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class BuggyLabelsGenerator {

    private BuggyLabelsGenerator() {
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
                "Usage: BuggyLabelsGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(args[0])
                .toAbsolutePath()
                .normalize();

        Path inventoryPath =
            repositoryPath.resolve(
                "isw2/datasets/java_class_inventory.csv"
            );

        Path lifecyclePath =
            repositoryPath.resolve(
                "isw2/datasets/defect_lifecycle.csv"
            );

        Path szzPath =
            repositoryPath.resolve(
                "isw2/datasets/"
                    + "szz_bug_introducing_changes.csv"
            );

        Path labelsOutput =
            repositoryPath.resolve(
                "isw2/datasets/buggy_labels.csv"
            );

        Path evidenceOutput =
            repositoryPath.resolve(
                "isw2/datasets/buggy_evidence.csv"
            );

        try {
            List<JavaFileInventoryEntry> inventory =
                JavaInventoryCsvReader.read(
                    inventoryPath
                );

            List<DefectLifecycle> lifecycles =
                DefectLifecycleCsvReader.read(
                    lifecyclePath
                );

            List<SzzBugIntroducingChange> szzRows =
                SzzCsvReader.read(
                    szzPath
                );

            long productionObservations =
                inventory.stream()
                    .filter(
                        row ->
                            row.sourceCategory()
                                == SourceCategory
                                    .PRODUCTION
                    )
                    .count();

            NativeClassLineageReader
                lineageReader =
                    new NativeClassLineageReader(
                        repositoryPath
                    );

            BuggyLabelResolver resolver =
                new BuggyLabelResolver(
                    lineageReader
                );

            BuggyLabelResolver.Result result =
                resolver.resolve(
                    lifecycles,
                    szzRows,
                    inventory
                );

            if (
                result.labels().size()
                    != productionObservations
            ) {
                throw new IOException(
                    "BUGGY labels do not match "
                        + "production inventory size."
                );
            }

            long buggyYes =
                result.labels()
                    .stream()
                    .filter(
                        BuggyLabelEntry
                            ::buggy
                    )
                    .count();

            long buggyNo =
                result.labels().size()
                    - buggyYes;

            BuggyLabelsCsvWriter.write(
                labelsOutput,
                result.labels()
            );

            BuggyEvidenceCsvWriter.write(
                evidenceOutput,
                result.evidence()
            );

            System.out.printf(
                "Production observations: %d%n",
                productionObservations
            );

            System.out.printf(
                "Candidate issue/release/class "
                    + "observations: %d%n",
                result.candidateObservations()
            );

            System.out.printf(
                "EXACT_BLAMED_PATH evidence: %d%n",
                result.exactMappings()
            );

            System.out.printf(
                "GIT_RENAME_LINEAGE evidence: %d%n",
                result.renameMappings()
            );

            System.out.printf(
                "Ambiguous safe lineage: %d%n",
                result.ambiguousMappings()
            );

            System.out.printf(
                "No safe class mapping: %d%n",
                result.unresolvedMappings()
            );

            System.out.printf(
                "Safe evidence rows: %d%n",
                result.evidence().size()
            );

            System.out.printf(
                "Distinct defects with safe mapping: %d%n",
                result.distinctDefectsWithMapping()
            );

            System.out.printf(
                "BUGGY=YES: %d%n",
                buggyYes
            );

            System.out.printf(
                "BUGGY=NO: %d%n",
                buggyNo
            );

            System.out.printf(
                "Git rename history reads: %d%n",
                lineageReader.historyReadCount()
            );

            System.out.printf(
                "Git rename cache entries: %d%n",
                lineageReader.cacheSize()
            );

            System.out.println();
            System.out.println(
                "BUGGY classes by release:"
            );

            Map<Integer, Long> yesByRelease =
                new TreeMap<>();

            for (BuggyLabelEntry row
                    : result.labels()) {

                if (!row.buggy()) {
                    continue;
                }

                yesByRelease.merge(
                    row.releaseIndex(),
                    1L,
                    Long::sum
                );
            }

            Map<Integer, String> versionByRelease =
                new TreeMap<>();

            for (BuggyLabelEntry row
                    : result.labels()) {

                versionByRelease.putIfAbsent(
                    row.releaseIndex(),
                    row.version()
                );
            }

            for (
                Map.Entry<Integer, String> entry
                    : versionByRelease.entrySet()
            ) {
                System.out.printf(
                    "  %2d %-18s %d%n",
                    entry.getKey(),
                    entry.getValue(),
                    yesByRelease.getOrDefault(
                        entry.getKey(),
                        0L
                    )
                );
            }

            System.out.printf(
                "%nBUGGY labels written to: %s%n",
                labelsOutput
            );

            System.out.printf(
                "BUGGY evidence written to: %s%n",
                evidenceOutput
            );

            return 0;

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "BUGGY label generation interrupted."
            );

            return 1;

        } catch (IOException exception) {

            System.err.printf(
                "BUGGY label generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}