package eu.zalvari.maven.changed.resources.core;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.junit.Before;
import org.junit.Test;

import eu.zalvari.maven.changed.resources.core.DifferentFiles;
import eu.zalvari.maven.changed.resources.core.Property;
import eu.zalvari.maven.changed.resources.mocks.LocalRepoMock;
import eu.zalvari.maven.changed.resources.mocks.RepoTest;

public abstract class DifferentFilesTest extends RepoTest {

    private static final String REFS_HEADS_FEATURE_2 = "refs/heads/feature/2";
    private static final String HEAD = "HEAD";
    private static final String FETCH_FILE = "fetch-file";
    private static final String DEVELOP = "refs/heads/develop";
    private static final String REMOTE_DEVELOP = "refs/remotes/origin/develop";

    @Before
    public void before() throws Exception {
        super.init();
        localRepoMock = new LocalRepoMock(true);
    }

    @Test
    public void listIncludingOnlyUncommited() throws Exception {
        LOCAL_DIR.resolve("file5").toFile().createNewFile();
        getLocalRepoMock().getGit().add().addFilepattern(".").call();
        Property.untracked.setValue(Boolean.FALSE.toString());
        Property.uncommited.setValue(Boolean.TRUE.toString());
        assertTrue(getInstance().get().stream().anyMatch(p -> p.toString().contains("file5")));
    }

    @Test
    public void listIncludingOnlyUntracked() throws Exception {
        LOCAL_DIR.resolve("file5").toFile().createNewFile();
        Property.uncommited.setValue(Boolean.FALSE.toString());
        Property.untracked.setValue(Boolean.TRUE.toString());
        assertTrue(getInstance().get().stream().anyMatch(p -> p.toString().contains("file5")));
    }

    @Test
    public void listWithCheckout() throws Exception {
        getLocalRepoMock().getGit().reset().setRef(HEAD).setMode(ResetCommand.ResetType.HARD).call();
        getLocalRepoMock().getGit().reset().setRef(REFS_HEADS_FEATURE_2).setMode(ResetCommand.ResetType.HARD).call();
        Property.baseBranch.setValue(REFS_HEADS_FEATURE_2);
        getInstance().get();
        assertTrue(consoleOut.toString().contains("Checking out base branch refs/heads/feature/2"));
    }

    @Test
    public void list() throws Exception {
        final DifferentFiles differentFiles = getInstance();
        final Set<Path> expected = new HashSet<>(Arrays.asList(
                        Paths.get(LOCAL_DIR.toString(), "/child2", "subchild2", "src", "resources", "file2"),
                        Paths.get(LOCAL_DIR.toString(), "/child2", "subchild2", "src", "resources", "file22"),
                        Paths.get(LOCAL_DIR.toString() + "/child3","src", "resources", "file1"),
                        Paths.get(LOCAL_DIR.toString() + "/child4", "pom.xml")
        ));
        assertEquals(expected, differentFiles.get());
    }

    @Test
    public void listInSubdir() throws Exception {
        Path workDir = LOCAL_DIR.resolve("child2");
        final DifferentFiles differentFiles = getInstance();
        final Set<Path> expected = new HashSet<>(Arrays.asList(
                        workDir.resolve("subchild2").resolve("src").resolve("resources").resolve("file2"),
                        workDir.resolve("subchild2").resolve("src").resolve("resources").resolve("file22"),
                        workDir.resolve("..").resolve("child3").resolve("src").resolve("resources").resolve("file1").normalize(),
                        workDir.resolve("..").resolve("child4").resolve("pom.xml").normalize()
        ));
        assertEquals(expected, differentFiles.get());
    }

    @Test
    public void listComparedToMergeBase() throws Exception {
        getLocalRepoMock().getGit().reset().setRef(REFS_HEADS_FEATURE_2).setMode(ResetCommand.ResetType.HARD).call();
        getLocalRepoMock().getGit().checkout().setName(REFS_HEADS_FEATURE_2).call();
        getLocalRepoMock().getGit().reset().setRef(HEAD).setMode(ResetCommand.ResetType.HARD).call();
        Property.baseBranch.setValue(REFS_HEADS_FEATURE_2);
        Property.compareToMergeBase.setValue("true");
        assertTrue(getInstance().get().stream().collect(toSet()).contains(LOCAL_DIR.resolve("feature2-only-file.txt")));
        assertTrue(consoleOut.toString().contains("ee64b9b863d3d30b429459cb3ccfaeac67e4efa1"));
    }

    @Test
    public void fetch() throws Exception {
        Git remoteGit = localRepoMock.getRemoteRepo().getGit();
        remoteGit.reset().setRef(DEVELOP).setMode(ResetCommand.ResetType.HARD).call();
        remoteGit.checkout().setName(DEVELOP).call();
        remoteGit.getRepository().getDirectory().toPath().resolve(FETCH_FILE).toFile().createNewFile();
        remoteGit.add().addFilepattern(".").call();
        remoteGit.commit().setMessage(FETCH_FILE).call();
        assertEquals(FETCH_FILE, remoteGit.log().setMaxCount(1).call().iterator().next().getFullMessage());
        Property.fetchReferenceBranch.setValue(Boolean.TRUE.toString());
        Property.referenceBranch.setValue(REMOTE_DEVELOP);
        getInstance().get();
        Git localGit = localRepoMock.getGit();
        localGit.reset().setRef(REMOTE_DEVELOP).setMode(ResetCommand.ResetType.HARD).call();
        localGit.checkout().setName(REMOTE_DEVELOP).call();
        assertEquals(FETCH_FILE, localGit.log().setMaxCount(1).call().iterator().next().getFullMessage());
    }

    @Test
    public void fetchNonExistent() throws Exception {
        Git remoteGit = localRepoMock.getRemoteRepo().getGit();
        remoteGit.reset().setRef(DEVELOP).setMode(ResetCommand.ResetType.HARD).call();
        remoteGit.checkout().setName(DEVELOP).call();
        remoteGit.getRepository().getDirectory().toPath().resolve(FETCH_FILE).toFile().createNewFile();
        remoteGit.add().addFilepattern(".").call();
        remoteGit.commit().setMessage(FETCH_FILE).call();
        Git localGit = localRepoMock.getGit();
        localGit.branchDelete().setBranchNames(DEVELOP).call();
        localGit.branchDelete().setBranchNames(REMOTE_DEVELOP).call();
        assertEquals(FETCH_FILE, remoteGit.log().setMaxCount(1).call().iterator().next().getFullMessage());
        Property.fetchReferenceBranch.setValue(Boolean.TRUE.toString());
        Property.referenceBranch.setValue(REMOTE_DEVELOP);
        getInstance().get();
        localGit.reset().setRef(REMOTE_DEVELOP).setMode(ResetCommand.ResetType.HARD).call();
        localGit.checkout().setName(REMOTE_DEVELOP).call();
        assertEquals(FETCH_FILE, localGit.log().setMaxCount(1).call().iterator().next().getFullMessage());
    }

    private boolean filterIgnored(Path p) {
        return !p.toString().contains("target") && !p.toString().contains(".iml");
    }

    protected abstract DifferentFiles getInstance() throws Exception;

}
