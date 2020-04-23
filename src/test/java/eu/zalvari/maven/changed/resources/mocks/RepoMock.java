package eu.zalvari.maven.changed.resources.mocks;

import java.io.File;

import org.assertj.core.util.Files;
import org.eclipse.jgit.api.Git;

public abstract class RepoMock implements AutoCloseable {

    public abstract File getRepoDir();

    public abstract Git getGit();

    @Override
    public void close() throws Exception {
        getGit().getRepository().close();
        getGit().close();
        delete(getRepoDir());
    }

    protected void delete(File f) {
        Files.delete(f);
    }

}
