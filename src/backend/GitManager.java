package backend;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GitManager {
    private Git git;

    /** Clone repo into given directory */
    public void cloneRepository(String url, String dir, String username, String password) throws GitAPIException {
        File repoDir = new File(dir);
        if (repoDir.exists()) {
            System.out.println("Directory already exists: " + dir);
            return;
        }

        System.out.println("Cloning repo from " + url + " into " + dir);
        this.git = Git.cloneRepository()
                .setURI(url.replace("https://", "https://" + username + ":" + password + "@"))
                .setDirectory(repoDir)
                .call();
    }

    /** Open existing repo */
    public void openRepository(String dir) throws IOException {
        File repoDir = new File(dir, ".git");
        if (!repoDir.exists()) {
            throw new IOException("No .git folder in " + dir);
        }

        Repository repo = new FileRepositoryBuilder()
                .setGitDir(repoDir)
                .build();
        this.git = new Git(repo);
        System.out.println("Opened repository at: " + repo.getDirectory().getAbsolutePath());
    }

    /**
     * Pull latest changes from remote.
     * Returns PullResult, and prints conflicts if they exist.
     */
    public PullResult pull(String username, String password) throws GitAPIException {
        if (git == null) {
            System.out.println("No repository opened.");
            return null;
        }

        PullResult pullResult = git.pull()
                .setRemote("origin")
                .setCredentialsProvider(
                        new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(username, password))
                .call();

        MergeResult mergeResult = pullResult.getMergeResult();
        if (mergeResult != null && mergeResult.getMergeStatus() == MergeStatus.CONFLICTING) {
            System.out.println("Merge conflicts detected: " + mergeResult.getConflicts());
        }

        return pullResult;
    }

    /** Commit given files (relative to repo/) */
    public void commitFiles(List<String> relativePaths, String message) throws GitAPIException {
        if (git == null) {
            System.out.println("No repository opened.");
            return;
        }

        for (String path : relativePaths) {
            git.add().addFilepattern(path).call();
        }
        git.commit().setMessage(message).call();
        System.out.println("Committed files: " + relativePaths);
    }

    /** Push changes */
    public void push(String username, String password) throws GitAPIException {
        if (git == null) {
            System.out.println("No repository opened.");
            return;
        }

        String remoteUrl = git.getRepository().getConfig().getString("remote", "origin", "url");
        System.out.println("Pushing to remote: " + remoteUrl);

        git.push()
                .setRemote("origin")
                .setCredentialsProvider(
                        new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(username, password))
                .call();
        System.out.println("Pushed successfully.");
    }
}
