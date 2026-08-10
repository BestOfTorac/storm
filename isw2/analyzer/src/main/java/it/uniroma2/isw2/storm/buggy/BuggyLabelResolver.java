package it.uniroma2.isw2.storm.buggy;

import it.uniroma2.isw2.storm.git.NativeClassLineageReader;
import it.uniroma2.isw2.storm.model.BuggyEvidenceEntry;
import it.uniroma2.isw2.storm.model.BuggyLabelEntry;
import it.uniroma2.isw2.storm.model.BuggyMappingStrategy;
import it.uniroma2.isw2.storm.model.DefectLifecycle;
import it.uniroma2.isw2.storm.model.JavaFileInventoryEntry;
import it.uniroma2.isw2.storm.model.SourceCategory;
import it.uniroma2.isw2.storm.model.SzzBugIntroducingChange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BuggyLabelResolver {

    private final NativeClassLineageReader
        lineageReader;

    public BuggyLabelResolver(
            NativeClassLineageReader lineageReader) {

        this.lineageReader =
            lineageReader;
    }

    public Result resolve(
            List<DefectLifecycle> lifecycles,
            List<SzzBugIntroducingChange> szzRows,
            List<JavaFileInventoryEntry> inventory)
            throws IOException,
            InterruptedException {

        List<JavaFileInventoryEntry> production =
            inventory.stream()
                .filter(
                    row ->
                        row.sourceCategory()
                            == SourceCategory.PRODUCTION
                )
                .sorted(
                    Comparator
                        .comparingInt(
                            JavaFileInventoryEntry
                                ::releaseIndex
                        )
                        .thenComparing(
                            JavaFileInventoryEntry
                                ::filePath
                        )
                )
                .toList();

        validateProductionInventory(
            production
        );

        Map<Integer, List<JavaFileInventoryEntry>>
            inventoryByRelease =
                new HashMap<>();

        Map<Integer, Set<String>>
            pathsByRelease =
                new HashMap<>();

        for (JavaFileInventoryEntry row
                : production) {

            inventoryByRelease
                .computeIfAbsent(
                    row.releaseIndex(),
                    ignored ->
                        new ArrayList<>()
                )
                .add(row);

            pathsByRelease
                .computeIfAbsent(
                    row.releaseIndex(),
                    ignored ->
                        new HashSet<>()
                )
                .add(
                    row.filePath()
                );
        }

        List<Integer> datasetReleaseIndices =
            inventoryByRelease
                .keySet()
                .stream()
                .sorted()
                .toList();

        Map<String, List<SzzBugIntroducingChange>>
            szzByIssue =
                new HashMap<>();

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

        List<BuggyEvidenceEntry> evidence =
            new ArrayList<>();

        int candidateObservations = 0;
        int exactMappings = 0;
        int renameMappings = 0;
        int ambiguousMappings = 0;
        int unresolvedMappings = 0;

        List<DefectLifecycle> relevant =
            lifecycles.stream()
                .filter(
                    DefectLifecycle
                        ::overlapsDataset
                )
                .sorted(
                    Comparator.comparing(
                        DefectLifecycle
                            ::issueKey
                    )
                )
                .toList();

        for (DefectLifecycle lifecycle
                : relevant) {

            List<SzzBugIntroducingChange>
                issueRows =
                    szzByIssue.get(
                        lifecycle.issueKey()
                    );

            if (
                issueRows == null
                    || issueRows.isEmpty()
            ) {
                throw new IOException(
                    "Missing SZZ evidence for "
                        + lifecycle.issueKey()
                );
            }

            Map<SeedKey, List<SzzBugIntroducingChange>>
                seedGroups =
                    groupSeeds(
                        issueRows
                    );

            for (
                Map.Entry<
                    SeedKey,
                    List<SzzBugIntroducingChange>
                > seedEntry
                    : seedGroups.entrySet()
            ) {
                SeedKey seed =
                    seedEntry.getKey();

                Set<String> lineagePaths =
                    buildLineage(
                        seed,
                        seedEntry.getValue()
                    );

                String sourceFileName =
                    fileName(
                        seed.blamedFilePath()
                    );

                for (int releaseIndex
                        : datasetReleaseIndices) {

                    if (
                        releaseIndex
                            < lifecycle
                                .effectiveIntroductionVersion()
                            || releaseIndex
                                >= lifecycle.fixVersion()
                    ) {
                        continue;
                    }

                    candidateObservations++;

                    Set<String> releasePaths =
                        pathsByRelease.get(
                            releaseIndex
                        );

                    List<JavaFileInventoryEntry>
                        releaseRows =
                            inventoryByRelease.get(
                                releaseIndex
                            );

                    JavaFileInventoryEntry
                        releaseRepresentative =
                            releaseRows.get(0);

                    if (
                        releasePaths.contains(
                            seed.blamedFilePath()
                        )
                    ) {
                        evidence.add(
                            evidence(
                                lifecycle,
                                releaseRepresentative,
                                seed,
                                seed.blamedFilePath(),
                                BuggyMappingStrategy
                                    .EXACT_BLAMED_PATH
                            )
                        );

                        exactMappings++;
                        continue;
                    }

                    List<String> candidates =
                        lineagePaths
                            .stream()
                            .filter(
                                releasePaths
                                    ::contains
                            )
                            .filter(
                                path ->
                                    sourceFileName
                                        .equals(
                                            fileName(path)
                                        )
                            )
                            .sorted()
                            .toList();

                    if (candidates.size() == 1) {

                        evidence.add(
                            evidence(
                                lifecycle,
                                releaseRepresentative,
                                seed,
                                candidates.get(0),
                                BuggyMappingStrategy
                                    .GIT_RENAME_LINEAGE
                            )
                        );

                        renameMappings++;

                    } else if (
                        candidates.size() > 1
                    ) {

                        ambiguousMappings++;

                    } else {

                        unresolvedMappings++;
                    }
                }
            }
        }

        if (
            candidateObservations
                != exactMappings
                    + renameMappings
                    + ambiguousMappings
                    + unresolvedMappings
        ) {
            throw new IOException(
                "BUGGY mapping accounting mismatch."
            );
        }

        if (
            evidence.size()
                != exactMappings
                    + renameMappings
        ) {
            throw new IOException(
                "BUGGY evidence accounting mismatch."
            );
        }

        Set<ObservationKey> buggyKeys =
            new HashSet<>();

        Set<String> mappedIssues =
            new HashSet<>();

        for (BuggyEvidenceEntry row
                : evidence) {

            ObservationKey key =
                new ObservationKey(
                    row.releaseIndex(),
                    row.filePath()
                );

            if (
                !pathsByRelease
                    .get(row.releaseIndex())
                    .contains(row.filePath())
            ) {
                throw new IOException(
                    "Mapped BUGGY evidence does not "
                        + "exist in production inventory: "
                        + row.releaseIndex()
                        + " "
                        + row.filePath()
                );
            }

            buggyKeys.add(key);
            mappedIssues.add(
                row.issueKey()
            );
        }

        List<BuggyLabelEntry> labels =
            new ArrayList<>();

        for (JavaFileInventoryEntry row
                : production) {

            ObservationKey key =
                new ObservationKey(
                    row.releaseIndex(),
                    row.filePath()
                );

            labels.add(
                new BuggyLabelEntry(
                    row.releaseIndex(),
                    row.version(),
                    row.commitId(),
                    row.filePath(),
                    buggyKeys.contains(key)
                )
            );
        }

        evidence.sort(
            Comparator
                .comparingInt(
                    BuggyEvidenceEntry
                        ::releaseIndex
                )
                .thenComparing(
                    BuggyEvidenceEntry
                        ::issueKey
                )
                .thenComparing(
                    BuggyEvidenceEntry
                        ::filePath
                )
                .thenComparing(
                    BuggyEvidenceEntry
                        ::blamedFilePath
                )
        );

        return new Result(
            List.copyOf(labels),
            List.copyOf(evidence),
            candidateObservations,
            exactMappings,
            renameMappings,
            ambiguousMappings,
            unresolvedMappings,
            mappedIssues.size()
        );
    }

    private Set<String> buildLineage(
            SeedKey seed,
            List<SzzBugIntroducingChange> rows)
            throws IOException,
            InterruptedException {

        Set<String> paths =
            new HashSet<>();

        paths.add(
            seed.blamedFilePath()
        );

        if (
            seed.fixedFilePath() != null
                && !seed.fixedFilePath()
                    .isBlank()
        ) {
            paths.add(
                seed.fixedFilePath()
            );
        }

        Set<String> parentCommits =
            new HashSet<>();

        for (SzzBugIntroducingChange row
                : rows) {

            parentCommits.add(
                row.parentCommitId()
            );
        }

        for (String parentCommit
                : parentCommits) {

            paths.addAll(
                lineageReader
                    .renameLineagePaths(
                        parentCommit,
                        seed.blamedFilePath()
                    )
            );
        }

        return Set.copyOf(paths);
    }

    private static Map<
        SeedKey,
        List<SzzBugIntroducingChange>
    > groupSeeds(
            List<SzzBugIntroducingChange> rows) {

        Map<
            SeedKey,
            List<SzzBugIntroducingChange>
        > groups =
            new LinkedHashMap<>();

        for (SzzBugIntroducingChange row
                : rows) {

            SeedKey key =
                new SeedKey(
                    row.issueKey(),
                    row.blamedFilePath(),
                    row.fixedFilePath()
                );

            groups
                .computeIfAbsent(
                    key,
                    ignored ->
                        new ArrayList<>()
                )
                .add(row);
        }

        return groups;
    }

    private static BuggyEvidenceEntry evidence(
            DefectLifecycle lifecycle,
            JavaFileInventoryEntry release,
            SeedKey seed,
            String mappedPath,
            BuggyMappingStrategy strategy) {

        return new BuggyEvidenceEntry(
            lifecycle.issueKey(),
            release.releaseIndex(),
            release.version(),
            release.commitId(),
            mappedPath,
            strategy,
            lifecycle
                .effectiveIntroductionVersion(),
            lifecycle.fixVersion(),
            lifecycle
                .introductionVersionSource()
                .name(),
            lifecycle
                .fixVersionSource()
                .name(),
            seed.blamedFilePath(),
            seed.fixedFilePath()
        );
    }

    private static void validateProductionInventory(
            List<JavaFileInventoryEntry> production)
            throws IOException {

        Set<ObservationKey> keys =
            new HashSet<>();

        for (JavaFileInventoryEntry row
                : production) {

            ObservationKey key =
                new ObservationKey(
                    row.releaseIndex(),
                    row.filePath()
                );

            if (!keys.add(key)) {
                throw new IOException(
                    "Duplicate production observation: "
                        + row.releaseIndex()
                        + " "
                        + row.filePath()
                );
            }
        }
    }

    private static String fileName(
            String filePath) {

        if (
            filePath == null
                || filePath.isBlank()
        ) {
            return "";
        }

        String normalized =
            filePath.replace(
                '\\',
                '/'
            );

        int lastSlash =
            normalized.lastIndexOf('/');

        if (lastSlash < 0) {
            return normalized;
        }

        return normalized.substring(
            lastSlash + 1
        );
    }

    public record Result(
        List<BuggyLabelEntry> labels,
        List<BuggyEvidenceEntry> evidence,
        int candidateObservations,
        int exactMappings,
        int renameMappings,
        int ambiguousMappings,
        int unresolvedMappings,
        int distinctDefectsWithMapping
    ) {
    }

    private record SeedKey(
        String issueKey,
        String blamedFilePath,
        String fixedFilePath
    ) {
    }

    private record ObservationKey(
        int releaseIndex,
        String filePath
    ) {
    }
}