package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.model.ReleaseCatalogEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NativeReleaseContainmentReader {

    private final Path repositoryPath;

    private final Map<String, Boolean>
        ancestryCache =
            new HashMap<>();

    private int ancestryReads = 0;

    public NativeReleaseContainmentReader(
            Path repositoryPath) {

        this.repositoryPath =
            Objects.requireNonNull(
                repositoryPath,
                "Repository path cannot be null."
            )
            .toAbsolutePath()
            .normalize();
    }

    public ReleaseCatalogEntry
            findEarliestContainingRelease(
                Set<String> commitIds,
                List<ReleaseCatalogEntry> releases)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
            commitIds,
            "Commit IDs cannot be null."
        );

        Objects.requireNonNull(
            releases,
            "Releases cannot be null."
        );

        List<ReleaseCatalogEntry> ordered =
            releases.stream()
                .sorted(
                    Comparator.comparingInt(
                        ReleaseCatalogEntry::index
                    )
                )
                .toList();

        for (ReleaseCatalogEntry release
                : ordered) {

            for (String commitId
                    : commitIds) {

                if (
                    isAncestor(
                        commitId,
                        release.gitCommitId()
                    )
                ) {
                    return release;
                }
            }
        }

        return null;
    }

    public int ancestryReadCount() {
        return ancestryReads;
    }

    private boolean isAncestor(
            String ancestor,
            String descendant)
            throws IOException, InterruptedException {

        String cacheKey =
            ancestor
                + "|"
                + descendant;

        Boolean cached =
            ancestryCache.get(
                cacheKey
            );

        if (cached != null) {
            return cached;
        }

        ProcessBuilder processBuilder =
            new ProcessBuilder(
                "git",
                "merge-base",
                "--is-ancestor",
                ancestor,
                descendant
            );

        processBuilder.directory(
            repositoryPath.toFile()
        );

        processBuilder.redirectErrorStream(
            true
        );

        Process process =
            processBuilder.start();

        /*
         * Drain the stream so Git can never block
         * waiting for its output buffer.
         */
        process.getInputStream()
            .readAllBytes();

        int exitCode =
            process.waitFor();

        ancestryReads++;

        boolean result;

        if (exitCode == 0) {

            result = true;

        } else if (exitCode == 1) {

            result = false;

        } else {

            throw new IOException(
                "git merge-base --is-ancestor "
                    + "failed with exit code "
                    + exitCode
                    + " for "
                    + ancestor
                    + " -> "
                    + descendant
            );
        }

        ancestryCache.put(
            cacheKey,
            result
        );

        return result;
    }
}