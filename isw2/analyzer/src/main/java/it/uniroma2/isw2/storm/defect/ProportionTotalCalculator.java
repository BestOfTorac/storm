package it.uniroma2.isw2.storm.defect;

import it.uniroma2.isw2.storm.model.JiraDefectInfo;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ProportionTotalCalculator {

    private ProportionTotalCalculator() {
        // Utility class.
    }

    public static Result calculate(
            List<ReleaseCatalogEntry> releases,
            List<JiraDefectInfo> defects) {

        Objects.requireNonNull(
            releases,
            "Releases cannot be null."
        );

        Objects.requireNonNull(
            defects,
            "Defects cannot be null."
        );

        if (releases.isEmpty()) {
            throw new IllegalArgumentException(
                "Release catalog cannot be empty."
            );
        }

        List<ReleaseCatalogEntry> orderedReleases =
            releases.stream()
                .sorted(
                    Comparator.comparingInt(
                        ReleaseCatalogEntry::index
                    )
                )
                .toList();

        Map<String, ReleaseCatalogEntry>
            releaseByVersion =
                buildReleaseMap(
                    orderedReleases
                );

        List<LifecycleObservation> observations =
            new ArrayList<>();

        double proportionSum = 0.0;
        int usableCount = 0;
        int completeCount = 0;

        for (JiraDefectInfo defect
                : defects) {

            ReleaseCatalogEntry openingVersion =
                findOpeningVersion(
                    orderedReleases,
                    defect.createdAt()
                        .toLocalDate()
                );

            ReleaseCatalogEntry introductionVersion =
                findEarliestOfficialVersion(
                    defect.affectedVersions(),
                    releaseByVersion
                );

            ReleaseCatalogEntry fixVersion =
                findEarliestOfficialVersion(
                    defect.fixVersions(),
                    releaseByVersion
                );

            boolean complete =
                introductionVersion != null
                    && openingVersion != null
                    && fixVersion != null;

            if (complete) {
                completeCount++;
            }

            boolean introductionAtOrBeforeOpening =
                complete
                    && introductionVersion.index()
                        <= openingVersion.index();

            boolean openingAtOrBeforeFix =
                complete
                    && openingVersion.index()
                        <= fixVersion.index();

            boolean introductionBeforeFix =
                complete
                    && introductionVersion.index()
                        < fixVersion.index();

            /*
             * Only available and consistent post-release
             * lifecycles contribute to P_TOTAL.
             */
            boolean usableForProportion =
                complete
                    && introductionAtOrBeforeOpening
                    && openingAtOrBeforeFix
                    && introductionBeforeFix;

            Double individualProportion =
                null;

            if (usableForProportion) {

                int denominator =
                    fixVersion.index()
                        - openingVersion.index();

                /*
                 * Replication-package rule:
                 * FV - OV is set to one when it is zero.
                 */
                if (denominator == 0) {
                    denominator = 1;
                }

                individualProportion =
                    (
                        fixVersion.index()
                            - introductionVersion.index()
                    )
                    / (double) denominator;

                proportionSum +=
                    individualProportion;

                usableCount++;
            }

            observations.add(
                new LifecycleObservation(
                    defect.key(),
                    introductionVersion,
                    openingVersion,
                    fixVersion,
                    complete,
                    introductionAtOrBeforeOpening,
                    openingAtOrBeforeFix,
                    introductionBeforeFix,
                    usableForProportion,
                    individualProportion
                )
            );
        }

        if (usableCount == 0) {
            throw new IllegalStateException(
                "No defect can contribute to "
                    + "Proportion Total."
            );
        }

        double pTotal =
            proportionSum
                / usableCount;

        return new Result(
            defects.size(),
            completeCount,
            usableCount,
            pTotal,
            List.copyOf(
                observations
            )
        );
    }

    private static Map<String, ReleaseCatalogEntry>
            buildReleaseMap(
                List<ReleaseCatalogEntry> releases) {

        Map<String, ReleaseCatalogEntry> result =
            new HashMap<>();

        for (ReleaseCatalogEntry release
                : releases) {

            String key =
                normalizeVersion(
                    release.version()
                );

            ReleaseCatalogEntry previous =
                result.putIfAbsent(
                    key,
                    release
                );

            if (previous != null) {
                throw new IllegalArgumentException(
                    "Duplicate release version: "
                        + release.version()
                );
            }
        }

        return Map.copyOf(
            result
        );
    }

    private static ReleaseCatalogEntry
            findOpeningVersion(
                List<ReleaseCatalogEntry> releases,
                LocalDate creationDate) {

        ReleaseCatalogEntry openingVersion =
            null;

        for (ReleaseCatalogEntry release
                : releases) {

            if (
                !release.releaseDate()
                    .isAfter(
                        creationDate
                    )
            ) {
                openingVersion =
                    release;
            }
        }

        return openingVersion;
    }

    private static ReleaseCatalogEntry
            findEarliestOfficialVersion(
                List<String> versions,
                Map<String, ReleaseCatalogEntry>
                    releaseByVersion) {

        ReleaseCatalogEntry earliest =
            null;

        for (String version
                : versions) {

            ReleaseCatalogEntry release =
                releaseByVersion.get(
                    normalizeVersion(
                        version
                    )
                );

            if (release == null) {
                continue;
            }

            if (
                earliest == null
                    || release.index()
                        < earliest.index()
            ) {
                earliest =
                    release;
            }
        }

        return earliest;
    }

    private static String normalizeVersion(
            String version) {

        return version
            .trim()
            .toLowerCase(
                Locale.ROOT
            );
    }

    public record LifecycleObservation(
        String issueKey,
        ReleaseCatalogEntry observedIntroductionVersion,
        ReleaseCatalogEntry openingVersion,
        ReleaseCatalogEntry fixVersion,
        boolean complete,
        boolean introductionAtOrBeforeOpening,
        boolean openingAtOrBeforeFix,
        boolean introductionBeforeFix,
        boolean usableForProportion,
        Double individualProportion
    ) {
    }

    public record Result(
        int eligibleDefects,
        int completeLifecycles,
        int usableDefects,
        double pTotal,
        List<LifecycleObservation> observations
    ) {
    }
}