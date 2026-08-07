package it.uniroma2.isw2.storm.git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class OfficialReleaseFirstParentReader {

    private final Path repositoryPath;

    public OfficialReleaseFirstParentReader(
            Path repositoryPath) {

        this.repositoryPath =
            Objects.requireNonNull(
                repositoryPath,
                "Repository path cannot be null."
            )
            .toAbsolutePath()
            .normalize();
    }

    public Set<String> read(
            List<String> releaseCommits)
            throws IOException, InterruptedException {

        Objects.requireNonNull(
            releaseCommits,
            "Release commits cannot be null."
        );

        Set<String> firstParentCommits =
            new HashSet<>();

        for (
            String releaseCommit
                : releaseCommits.stream()
                    .filter(
                        commit ->
                            commit != null
                                && !commit.isBlank()
                    )
                    .distinct()
                    .toList()
        ) {
            String output =
                execute(
                    List.of(
                        "git",
                        "rev-list",
                        "--first-parent",
                        releaseCommit
                    )
                );

            for (
                String line
                    : output.split("\\R")
            ) {
                String commit =
                    line.trim();

                if (!commit.isBlank()) {
                    firstParentCommits.add(
                        commit
                    );
                }
            }
        }

        return Set.copyOf(
            firstParentCommits
        );
    }

    private String execute(
            List<String> command)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
            new ProcessBuilder(
                command
            );

        processBuilder.directory(
            repositoryPath.toFile()
        );

        Process process =
            processBuilder.start();

        byte[] stdout =
            process
                .getInputStream()
                .readAllBytes();

        byte[] stderr =
            process
                .getErrorStream()
                .readAllBytes();

        int exitCode =
            process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                "Git command failed with exit code "
                    + exitCode
                    + ": "
                    + String.join(
                        " ",
                        command
                    )
                    + System.lineSeparator()
                    + new String(
                        stderr,
                        StandardCharsets.UTF_8
                    )
            );
        }

        return new String(
            stdout,
            StandardCharsets.UTF_8
        );
    }
}