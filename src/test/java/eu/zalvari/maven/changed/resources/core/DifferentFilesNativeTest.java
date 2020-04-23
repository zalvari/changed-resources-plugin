package eu.zalvari.maven.changed.resources.core;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.Guice;

import eu.zalvari.maven.changed.resources.core.DifferentFiles;
import eu.zalvari.maven.changed.resources.core.DifferentFilesNative;
import eu.zalvari.maven.changed.resources.mocks.ModuleMock;

@RunWith(MockitoJUnitRunner.class)
public class DifferentFilesNativeTest extends DifferentFilesTest {

    protected DifferentFiles getInstance() throws Exception {
        return Guice.createInjector(ModuleMock.module(getLocalRepoMock().getRepoDir().toString()))
                .getInstance(DifferentFilesNative.class);
    }

}
