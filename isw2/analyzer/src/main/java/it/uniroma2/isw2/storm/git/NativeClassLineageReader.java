package it.uniroma2.isw2.storm.git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NativeClassLineageReader {

    private final Path repositoryPath;

    private final Map<String, Set<String>>
        cache =
            new HashMap<>();

    private int historyReadCount;

    public NativeClassLineageReader(
            Path repositoryPath) {

        this.repositoryPath =
            repositoryPath;
    }

    public Set<String> renameLineagePaths(
            String startCommitId,
            String filePath)
            throws IOException,
            InterruptedException {

        String key =
            startCommitId
                + "|"
                + filePath;

        Set<String> cached =
            cache.get(key);

        if (cached != null) {
            return cached;
        }

        List<String> command =
            List.of(
                "git",
                "-c",
                "core.quotepath=false",
                "-c",
                "diff.renameLimit=0",
                "log",
                startCommitId,
                "--follow",
                "--find-renames",
                "--name-status",
                "--format=",
                "--",
                filePath
            );

        List<String> lines =
            execute(command);

        Set<String> paths =
            new HashSet<>();

        paths.add(filePath);

        for (String line : lines) {

            if (line.isBlank()) {
                continue;
            }

            String[] columns =
                line.split(
                    "\t",
                    -1
                );

            if (columns.length < 3) {
                continue;
            }

            String status =
                columns[0];

            /*
             * Only real Git rename records are accepted.
             *
             * Copy records (C...) are deliberately excluded:
             * copying a file does not prove class identity.
             */
            if (!status.startsWith("R")) {
                continue;
            }

            String oldPath =
                columns[1];

            String newPath =
                columns[2];

            if (!oldPath.isBlank()) {
                paths.add(oldPath);
            }

            if (!newPath.isBlank()) {
                paths.add(newPath);
            }
        }

        Set<String> result =
            Set.copyOf(paths);

        cache.put(
            key,
            result
        );

        historyReadCount++;

        return result;
    }

    public int historyReadCount() {
        return historyReadCount;
    }

    public int cacheSize() {
        return cache.size();
    }

    private List<String> execute(
            List<String> command)
            throws IOException,
            InterruptedException {

        ProcessBuilder processBuilder =
            new ProcessBuilder(command);

        processBuilder.directory(
            repositoryPath.toFile()
        );

        processBuilder
            .environment()
            .put(
                "LC_ALL",
                "C"
            );

        processBuilder.redirectErrorStream(
            true
        );

        Process process =
            processBuilder.start();

        List<String> lines =
            new ArrayList<>();

        try (
            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        int exitCode =
            process.waitFor();

        if (exitCode != 0) {
            throw new IOException(
                "Git class-lineage command failed "
                    + "with exit code "
                    + exitCode
                    + ": "
                    + String.join(
                        " ",
                        command
                    )
            );
        }

        return List.copyOf(lines);
    }
}