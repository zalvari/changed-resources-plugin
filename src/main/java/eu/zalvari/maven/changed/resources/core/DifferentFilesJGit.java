package eu.zalvari.maven.changed.resources.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DifferentFilesJGit implements DifferentFiles {

    private static final String HEAD = "HEAD";
    private static final String REFS_REMOTES = "refs/remotes/";
    private static final String REFS_HEADS = "refs/heads/";
    @Inject
    private Git git;
    @Inject
    private Configuration configuration;
    @Inject
    private Logger logger;

    public Set<Path> get() throws IOException {
        try {
            fetch();
            checkout();
            RevCommit base = getBranchHead(configuration.baseBranch);
            final Set<Path> paths;
            try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                treeWalk.addTree(base.getTree());
                treeWalk.addTree(resolveReference(base).getTree());
                treeWalk.setFilter(TreeFilter.ANY_DIFF);
                treeWalk.setRecursive(true);
                final Path gitDir = Paths.get(git.getRepository().getDirectory().getCanonicalPath()).getParent();
                paths = getDiff(treeWalk, gitDir);
                if (configuration.uncommited) {
                    paths.addAll(getUncommitedChanges(gitDir));
                }
                if (configuration.untracked) {
                    paths.addAll(getUntrackedChanges(gitDir));
                }
            }
            git.getRepository().close();
            git.close();
            return paths;
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
    }

    private void checkout() throws IOException, GitAPIException {
        if (!HEAD.equals(configuration.baseBranch)
                        && !git.getRepository().getFullBranch().equals(configuration.baseBranch)) {
            logger.info("Checking out base branch " + configuration.baseBranch + "...");
            git.checkout().setName(configuration.baseBranch).call();
        }
    }

    private void fetch() throws GitAPIException {
        if (configuration.fetchReferenceBranch) {
            fetch(configuration.referenceBranch);
        }
        if (configuration.fetchBaseBranch) {
            fetch(configuration.baseBranch);
        }
    }

    private void fetch(String branchName) throws GitAPIException {
        logger.info("Fetching branch " + branchName);
        if (!branchName.startsWith(REFS_REMOTES)) {
            throw new IllegalArgumentException("Branch name '" + branchName + "' is not tracking branch name since it" +
                            " does not start " + REFS_REMOTES);
        }
        String remoteName = extractRemoteName(branchName);
        String shortName = extractShortName(remoteName, branchName);
        git.fetch().setRemote(remoteName).setRefSpecs(new RefSpec(REFS_HEADS + shortName + ":" + branchName)).call();
    }

    private String extractRemoteName(String branchName) {
        return branchName.split("/")[2];
    }

    private String extractShortName(String remoteName, String branchName) {
        return branchName.replaceFirst(REFS_REMOTES + remoteName + "/", "");
    }

    private RevCommit getMergeBase(RevCommit baseCommit, RevCommit referenceHeadCommit) throws IOException {
        RevWalk walk = new RevWalk(git.getRepository());
        walk.setRevFilter(RevFilter.MERGE_BASE);
        walk.markStart(walk.lookupCommit(baseCommit));
        walk.markStart(walk.lookupCommit(referenceHeadCommit));
        RevCommit commit = walk.next();
        walk.close();
        logger.info("Using merge base of id: " + commit.getId());
        return commit;
    }

    private Set<Path> getDiff(TreeWalk treeWalk, Path gitDir) throws IOException {
        final Set<Path> paths = new HashSet<>();
        while (treeWalk.next()) {
            paths.add(gitDir.resolve(treeWalk.getPathString()).normalize());
        }
        return paths;
    }

    private RevCommit getBranchHead(String branchName) throws IOException {
        final RevWalk walk = new RevWalk(git.getRepository());
        ObjectId resolvedId = git.getRepository().resolve(branchName);
        if (resolvedId == null) {
            throw new IllegalArgumentException("Git rev str '" + branchName + "' not found.");
        }
        RevCommit commit = walk.parseCommit(resolvedId);
        walk.close();
        logger.info("Head of branch " + branchName + " is commit of id: " + commit.getId());
        return commit;
    }

    private Set<Path> getUncommitedChanges(Path gitDir) throws GitAPIException {
        return git.status().call().getUncommittedChanges().stream()
                        .map(gitDir::resolve).map(Path::normalize).collect(Collectors.toSet());
    }

    private Set<Path> getUntrackedChanges(Path gitDir) throws GitAPIException {
        return git.status().call().getUntracked().stream()
                        .map(gitDir::resolve).map(Path::normalize).collect(Collectors.toSet());
    }

    private RevCommit resolveReference(RevCommit base) throws IOException {
        RevCommit refHead = getBranchHead(configuration.referenceBranch);
        if (configuration.compareToMergeBase) {
            return getMergeBase(base, refHead);
        } else {
            return refHead;
        }
    }

}
