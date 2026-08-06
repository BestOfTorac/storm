package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.model.GitJavaSourceFile;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class GitJavaSourceReader
        implements AutoCloseable {

    private final Repository repository;

    private GitJavaSourceReader(
            Repository repository) {

        this.repository = repository;
    }

    public static GitJavaSourceReader open(
            Path repositoryRoot)
            throws IOException {

        Path gitDirectory =
            repositoryRoot.resolve(".git");

        if (!Files.exists(gitDirectory)) {
            throw new IOException(
                "Git directory not found: "
                    + gitDirectory
            );
        }

        Repository repository =
            new FileRepositoryBuilder()
                .setGitDir(gitDirectory.toFile())
                .setMustExist(true)
                .build();

        return new GitJavaSourceReader(repository);
    }

    public List<GitJavaSourceFile>
            readJavaFilesAtCommit(
                String commitId,
                Predicate<String> pathSelector)
            throws IOException {

        Objects.requireNonNull(
            commitId,
            "Commit ID cannot be null."
        );

        Objects.requireNonNull(
            pathSelector,
            "Path selector cannot be null."
        );

        String normalizedCommitId =
            commitId.trim();

        if (normalizedCommitId.isBlank()) {
            throw new IOException(
                "Commit ID cannot be blank."
            );
        }

        ObjectId commitObjectId =
            repository.resolve(
                normalizedCommitId
            );

        if (commitObjectId == null) {
            throw new IOException(
                "Unable to resolve commit: "
                    + normalizedCommitId
            );
        }

        List<GitJavaSourceFile> sources =
            new ArrayList<>();

        try (
            RevWalk revWalk =
                new RevWalk(repository);

            TreeWalk treeWalk =
                new TreeWalk(repository)
        ) {
            RevCommit commit =
                revWalk.parseCommit(
                    commitObjectId
                );

            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(
                PathSuffixFilter.create(".java")
            );

            while (treeWalk.next()) {
                String filePath =
                    treeWalk.getPathString();

                if (!pathSelector.test(filePath)) {
                    continue;
                }

                ObjectId blobId =
                    treeWalk.getObjectId(0);

                ObjectLoader loader =
                    repository.open(
                        blobId,
                        Constants.OBJ_BLOB
                    );

                String content;

                try (
                    InputStream inputStream =
                        loader.openStream()
                ) {
                    content = new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8
                    );
                }

                sources.add(
                    new GitJavaSourceFile(
                        filePath,
                        content
                    )
                );
            }
        }

        sources.sort(
            Comparator.comparing(
                GitJavaSourceFile::filePath
            )
        );

        return List.copyOf(sources);
    }

    @Override
    public void close() {
        repository.close();
    }
}