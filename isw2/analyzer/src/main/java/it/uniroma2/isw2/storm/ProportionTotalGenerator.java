package it.uniroma2.isw2.storm;

import it.uniroma2.isw2.storm.defect.DefectCatalogCsvReader;
import it.uniroma2.isw2.storm.defect.ProportionTotalCalculator;
import it.uniroma2.isw2.storm.model.JiraDefectInfo;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.release.ReleaseCatalogCsvReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class ProportionTotalGenerator {

    private ProportionTotalGenerator() {
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
                "Usage: ProportionTotalGenerator "
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

        try {
            List<ReleaseCatalogEntry> releases =
                ReleaseCatalogCsvReader.read(
                    releaseCatalogPath
                );

            List<JiraDefectInfo> defects =
                DefectCatalogCsvReader.read(
                    defectCatalogPath
                );

            ProportionTotalCalculator.Result result =
                ProportionTotalCalculator.calculate(
                    releases,
                    defects
                );

            List<Double> pValues =
                result.observations()
                    .stream()
                    .filter(
                        ProportionTotalCalculator
                            .LifecycleObservation
                            ::usableForProportion
                    )
                    .map(
                        ProportionTotalCalculator
                            .LifecycleObservation
                            ::individualProportion
                    )
                    .sorted()
                    .toList();

            double minimum =
                pValues.get(0);

            double maximum =
                pValues.get(
                    pValues.size() - 1
                );

            double median;

            if (
                pValues.size() % 2 == 1
            ) {
                median =
                    pValues.get(
                        pValues.size() / 2
                    );

            } else {

                double left =
                    pValues.get(
                        pValues.size() / 2 - 1
                    );

                double right =
                    pValues.get(
                        pValues.size() / 2
                    );

                median =
                    (left + right) / 2.0;
            }

            System.out.printf(
                Locale.ROOT,
                "Official releases: %d%n",
                releases.size()
            );

            System.out.printf(
                Locale.ROOT,
                "Eligible Jira defects: %d%n",
                result.eligibleDefects()
            );

            System.out.printf(
                Locale.ROOT,
                "Complete official IV/OV/FV: %d%n",
                result.completeLifecycles()
            );

            System.out.printf(
                Locale.ROOT,
                "Usable defects for P_TOTAL: %d%n",
                result.usableDefects()
            );

            System.out.printf(
                Locale.ROOT,
                "P_TOTAL (mean): %.15f%n",
                result.pTotal()
            );

            System.out.printf(
                Locale.ROOT,
                "P median: %.15f%n",
                median
            );

            System.out.printf(
                Locale.ROOT,
                "P minimum: %.15f%n",
                minimum
            );

            System.out.printf(
                Locale.ROOT,
                "P maximum: %.15f%n",
                maximum
            );

            return 0;

        } catch (
                IOException
                | IllegalArgumentException
                | IllegalStateException exception) {

            System.err.printf(
                "Proportion Total generation "
                    + "failed: %s%n",
                exception.getMessage()
            );

            return 1;
        }
    }
}