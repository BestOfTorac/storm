package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.defect.DefectCatalogCsvWriter;
import it.uniroma2.isw2.storm.jira.JiraDefectClient;
import it.uniroma2.isw2.storm.model.JiraDefectInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DefectCatalogGenerator {

    private static final String JIRA_PROJECT =
        "STORM";

    private DefectCatalogGenerator() {
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
                "Usage: DefectCatalogGenerator "
                    + "<repository-path>"
            );

            return 2;
        }

        Path repositoryPath =
            Path.of(
                    args[0]
                )
                .toAbsolutePath()
                .normalize();

        Path outputPath =
            repositoryPath.resolve(
                "isw2/datasets/defect_catalog.csv"
            );

        try {
            List<JiraDefectInfo> defects =
                JiraDefectClient
                    .apacheJira()
                    .fetchFixedBugs(
                        JIRA_PROJECT
                    );

            DefectCatalogCsvWriter.write(
                outputPath,
                defects
            );

            long withAffectedVersions =
                defects.stream()
                    .filter(
                        defect ->
                            !defect
                                .affectedVersions()
                                .isEmpty()
                    )
                    .count();

            long withFixVersions =
                defects.stream()
                    .filter(
                        defect ->
                            !defect
                                .fixVersions()
                                .isEmpty()
                    )
                    .count();

            long withBoth =
                defects.stream()
                    .filter(
                        defect ->
                            !defect
                                .affectedVersions()
                                .isEmpty()
                                && !defect
                                    .fixVersions()
                                    .isEmpty()
                    )
                    .count();

            long withoutVersionMetadata =
                defects.stream()
                    .filter(
                        defect ->
                            defect
                                .affectedVersions()
                                .isEmpty()
                                && defect
                                    .fixVersions()
                                    .isEmpty()
                    )
                    .count();

            System.out.printf(
                "Eligible fixed bugs: %d%n",
                defects.size()
            );

            System.out.printf(
                "With affected versions: %d%n",
                withAffectedVersions
            );

            System.out.printf(
                "With fix versions: %d%n",
                withFixVersions
            );

            System.out.printf(
                "With both version types: %d%n",
                withBoth
            );

            System.out.printf(
                "Without version metadata: %d%n",
                withoutVersionMetadata
            );

            System.out.printf(
                "Defect catalog written to: %s%n",
                outputPath
            );

            return 0;

        } catch (InterruptedException exception) {

            Thread.currentThread()
                .interrupt();

            System.err.println(
                "Defect catalog generation interrupted."
            );

            return 1;

        } catch (
                IOException
                | IllegalArgumentException exception) {

            System.err.printf(
                "Defect catalog generation failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}