package it.uniroma2.isw2.storm.buggy;

import it.uniroma2.isw2.storm.git.NativeReleaseContainmentReader;
import it.uniroma2.isw2.storm.model.DefectLifecycle;
import it.uniroma2.isw2.storm.model.JiraDefectInfo;
import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;
import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DefectLifecycleResolver {

    private DefectLifecycleResolver() {
        // Utility class.
    }

    public static Result resolve(
            List<ReleaseCatalogEntry> releases,
            List<JiraDefectInfo> defects,
            List<SzzBugIntroducingChange> szzRows,
            double pTotal,
            NativeReleaseContainmentReader containmentReader)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
            releases,
            "Releases cannot be null."
        );

        Objects.requireNonNull(
            defects,
            "Defects cannot be null."
        );

        Objects.requireNonNull(
            szzRows,
            "SZZ rows cannot be null."
        );

        Objects.requireNonNull(
            containmentReader,
            "Containment reader cannot be null."
        );

        if (
            !Double.isFinite(pTotal)
                || pTotal <= 0.0
        ) {
            throw new IllegalArgumentException(
                "P_TOTAL must be finite and positive."
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

        Map<String, JiraDefectInfo>
            defectByIssue =
                buildDefectMap(
                    defects
                );

        Map<String, List<SzzBugIntroducingChange>>
            szzByIssue =
                new LinkedHashMap<>();

        for (SzzBugIntroducingChange row
                : szzRows) {

            szzByIssue
                .computeIfAbsent(
                    row.issueKey(),
                    ignored ->
                        new ArrayList<>()
                )
                .add(row);
        }

        List<DefectLifecycle> lifecycles =
            new ArrayList<>();

        int jiraFixVersionCount = 0;
        int gitContainmentCount = 0;

        int jiraAffectedVersionCount = 0;
        int proportionCount = 0;

        for (
            Map.Entry<
                String,
                List<SzzBugIntroducingChange>
            > entry
                : szzByIssue.entrySet()
        ) {
            String issueKey =
                entry.getKey();

            List<SzzBugIntroducingChange>
                issueSzzRows =
                    entry.getValue();

            JiraDefectInfo defect =
                defectByIssue.get(
                    issueKey
                );

            if (defect == null) {
                throw new IllegalStateException(
                    "SZZ issue not found in "
                        + "defect catalog: "
                        + issueKey
                );
            }

            ReleaseCatalogEntry openingVersion =
                findOpeningVersion(
                    orderedReleases,
                    defect.createdAt()
                        .toLocalDate()
                );

            if (openingVersion == null) {
                throw new IllegalStateException(
                    "Unable to determine OV for "
                        + issueKey
                );
            }

            ReleaseCatalogEntry fixVersion =
                findEarliestOfficialVersion(
                    defect.fixVersions(),
                    releaseByVersion
                );

            DefectLifecycle.FixVersionSource
                fixVersionSource;

            if (fixVersion != null) {

                fixVersionSource =
                    DefectLifecycle
                        .FixVersionSource
                        .JIRA_FIX_VERSION;

                jiraFixVersionCount++;

            } else {

                Set<String> szzFixCommits =
                    new HashSet<>();

                for (SzzBugIntroducingChange row
                        : issueSzzRows) {

                    szzFixCommits.add(
                        row.fixCommitId()
                    );
                }

                fixVersion =
                    containmentReader
                        .findEarliestContainingRelease(
                            szzFixCommits,
                            orderedReleases
                        );

                if (fixVersion == null) {
                    throw new IllegalStateException(
                        "Unable to determine FV "
                            + "for "
                            + issueKey
                    );
                }

                fixVersionSource =
                    DefectLifecycle
                        .FixVersionSource
                        .GIT_CONTAINMENT;

                gitContainmentCount++;
            }

            if (
                openingVersion.index()
                    > fixVersion.index()
            ) {
                throw new IllegalStateException(
                    "OV is after FV for "
                        + issueKey
                        + ": OV="
                        + openingVersion.index()
                        + ", FV="
                        + fixVersion.index()
                );
            }

            ReleaseCatalogEntry observedIv =
                findEarliestOfficialVersion(
                    defect.affectedVersions(),
                    releaseByVersion
                );

            boolean observedIvConsistent =
                observedIv != null
                    && observedIv.index()
                        <= openingVersion.index()
                    && observedIv.index()
                        < fixVersion.index();

            DefectLifecycle
                .IntroductionVersionSource
                    ivSource;

            Double lav = null;
            Double pUsed = null;

            int effectiveIv;

            if (observedIvConsistent) {

                ivSource =
                    DefectLifecycle
                        .IntroductionVersionSource
                        .JIRA_AFFECTED_VERSION;

                effectiveIv =
                    observedIv.index();

                jiraAffectedVersionCount++;

            } else {

                ivSource =
                    DefectLifecycle
                        .IntroductionVersionSource
                        .PROPORTION_TOTAL;

                int distance =
                    fixVersion.index()
                        - openingVersion.index();

                if (distance == 0) {
                    distance = 1;
                }

                lav =
                    fixVersion.index()
                        - (
                            pTotal
                                * distance
                        );

                effectiveIv =
                    (int) Math.ceil(
                        lav
                    );

                if (effectiveIv < 1) {
                    effectiveIv = 1;
                }

                if (
                    effectiveIv
                        >= fixVersion.index()
                ) {
                    throw new IllegalStateException(
                        "Estimated IV is not before "
                            + "FV for "
                            + issueKey
                            + ": IV="
                            + effectiveIv
                            + ", FV="
                            + fixVersion.index()
                    );
                }

                pUsed =
                    pTotal;

                proportionCount++;
            }

            boolean overlapsDataset =
                overlapsDataset(
                    orderedReleases,
                    effectiveIv,
                    fixVersion.index()
                );

            Set<String> distinctFixCommits =
                new HashSet<>();

            for (SzzBugIntroducingChange row
                    : issueSzzRows) {

                distinctFixCommits.add(
                    row.fixCommitId()
                );
            }

            lifecycles.add(
                new DefectLifecycle(
                    issueKey,
                    observedIv == null
                        ? null
                        : observedIv.index(),
                    observedIv == null
                        ? ""
                        : observedIv.version(),
                    openingVersion.index(),
                    openingVersion.version(),
                    fixVersion.index(),
                    fixVersion.version(),
                    fixVersionSource,
                    ivSource,
                    lav,
                    effectiveIv,
                    pUsed,
                    overlapsDataset,
                    distinctFixCommits.size(),
                    issueSzzRows.size(),
                    String.join(
                        " | ",
                        defect.affectedVersions()
                    ),
                    String.join(
                        " | ",
                        defect.fixVersions()
                    )
                )
            );
        }

        lifecycles.sort(
            Comparator.comparing(
                DefectLifecycle::issueKey
            )
        );

        return new Result(
            List.copyOf(
                lifecycles
            ),
            jiraFixVersionCount,
            gitContainmentCount,
            jiraAffectedVersionCount,
            proportionCount
        );
    }

    private static Map<String, ReleaseCatalogEntry>
            buildReleaseMap(
                List<ReleaseCatalogEntry> releases) {

        Map<String, ReleaseCatalogEntry> map =
            new HashMap<>();

        for (ReleaseCatalogEntry release
                : releases) {

            String key =
                normalizeVersion(
                    release.version()
                );

            ReleaseCatalogEntry previous =
                map.putIfAbsent(
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

        return Map.copyOf(map);
    }

    private static Map<String, JiraDefectInfo>
            buildDefectMap(
                List<JiraDefectInfo> defects) {

        Map<String, JiraDefectInfo> map =
            new HashMap<>();

        for (JiraDefectInfo defect
                : defects) {

            JiraDefectInfo previous =
                map.putIfAbsent(
                    defect.key(),
                    defect
                );

            if (previous != null) {
                throw new IllegalArgumentException(
                    "Duplicate Jira defect: "
                        + defect.key()
                );
            }
        }

        return Map.copyOf(map);
    }

    private static ReleaseCatalogEntry
            findOpeningVersion(
                List<ReleaseCatalogEntry> releases,
                LocalDate creationDate) {

        ReleaseCatalogEntry result =
            null;

        for (ReleaseCatalogEntry release
                : releases) {

            if (
                !release.releaseDate()
                    .isAfter(
                        creationDate
                    )
            ) {
                result =
                    release;
            }
        }

        return result;
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

    private static boolean overlapsDataset(
            List<ReleaseCatalogEntry> releases,
            int introductionVersion,
            int fixVersion) {

        for (ReleaseCatalogEntry release
                : releases) {

            if (
                release.includedInDataset()
                    && release.index()
                        >= introductionVersion
                    && release.index()
                        < fixVersion
            ) {
                return true;
            }
        }

        return false;
    }

    private static String normalizeVersion(
            String version) {

        return version
            .trim()
            .toLowerCase(
                Locale.ROOT
            );
    }

    public record Result(
        List<DefectLifecycle> lifecycles,
        int jiraFixVersions,
        int gitContainmentFixVersions,
        int jiraIntroductionVersions,
        int proportionIntroductionVersions
    ) {
    }
}