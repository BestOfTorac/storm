package it.uniroma2.isw2.storm.git;

import it.uniroma2.isw2.storm.model.GitTagInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RepositoryInspector implements AutoCloseable {

    private final Repository repository;

    private RepositoryInspector(Repository repository) {
        this.repository = repository;
    }

    public static RepositoryInspector open(Path repositoryRoot)
            throws IOException {

        Path gitDirectory = repositoryRoot.resolve(".git");

        if (!Files.exists(gitDirectory)) {
            throw new IOException(
                "Git directory not found: " + gitDirectory
            );
        }

        Repository repository = new FileRepositoryBuilder()
            .setGitDir(gitDirectory.toFile())
            .setMustExist(true)
            .build();

        return new RepositoryInspector(repository);
    }

    public String currentBranch() throws IOException {
        return repository.getBranch();
    }

    public String headCommitId() throws IOException {
        ObjectId head = repository.resolve(Constants.HEAD);

        if (head == null) {
            throw new IOException(
                "Unable to resolve the repository HEAD."
            );
        }

        return head.name();
    }

    public Map<String, String> remoteUrls() {
        Config configuration = repository.getConfig();

        List<String> remoteNames = configuration
            .getSubsections("remote")
            .stream()
            .sorted()
            .toList();

        Map<String, String> remotes = new LinkedHashMap<>();

        for (String remoteName : remoteNames) {
            String url = configuration.getString(
                "remote",
                remoteName,
                "url"
            );

            if (url != null && !url.isBlank()) {
                remotes.put(remoteName, url);
            }
        }

        return remotes;
    }

    public List<GitTagInfo> readTags()
            throws GitAPIException, IOException {

        List<GitTagInfo> tags = new ArrayList<>();

        try (
            Git git = new Git(repository);
            RevWalk revWalk = new RevWalk(repository)
        ) {
            for (Ref tagReference : git.tagList().call()) {
                Ref peeledReference = repository
                    .getRefDatabase()
                    .peel(tagReference);

                ObjectId commitId =
                    peeledReference.getPeeledObjectId();

                if (commitId == null) {
                    commitId = tagReference.getObjectId();
                }

                if (commitId == null) {
                    continue;
                }

                RevCommit commit =
                    revWalk.parseCommit(commitId);

                String tagName =
                    Repository.shortenRefName(
                        tagReference.getName()
                    );

                Instant commitDate =
                    Instant.ofEpochSecond(
                        commit.getCommitTime()
                    );

                tags.add(
                    new GitTagInfo(
                        tagName,
                        commit.name(),
                        commitDate
                    )
                );
            }
        }

        tags.sort(
            Comparator
                .comparing(GitTagInfo::commitDate)
                .thenComparing(GitTagInfo::name)
        );

        return List.copyOf(tags);
    }

    public List<String> readJavaFilePathsAtCommit(
            String commitId)
            throws IOException {

        Objects.requireNonNull(
            commitId,
            "Commit ID cannot be null."
        );

        String normalizedCommitId = commitId.trim();

        if (normalizedCommitId.isBlank()) {
            throw new IOException(
                "Commit ID cannot be blank."
            );
        }

        ObjectId commitObjectId =
            repository.resolve(normalizedCommitId);

        if (commitObjectId == null) {
            throw new IOException(
                "Unable to resolve commit: "
                    + normalizedCommitId
            );
        }

        List<String> javaFilePaths =
            new ArrayList<>();

        try (
            RevWalk revWalk =
                new RevWalk(repository);

            TreeWalk treeWalk =
                new TreeWalk(repository)
        ) {
            RevCommit commit =
                revWalk.parseCommit(commitObjectId);

            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(
                PathSuffixFilter.create(".java")
            );

            while (treeWalk.next()) {
                javaFilePaths.add(
                    treeWalk.getPathString()
                );
            }
        }

        javaFilePaths.sort(String::compareTo);

        return List.copyOf(javaFilePaths);
    }

    @Override
    public void close() {
        repository.close();
    }
}