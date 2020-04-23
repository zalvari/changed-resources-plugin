package eu.zalvari.maven.changed.resources.core;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;

import com.google.inject.Guice;

import eu.zalvari.maven.changed.resources.core.ChangedFiles;
import eu.zalvari.maven.changed.resources.core.GuiceModule;
import eu.zalvari.maven.changed.resources.mocks.MavenSessionMock;
import eu.zalvari.maven.changed.resources.mocks.RepoTest;

public class ChangedFilesTest extends RepoTest {

    @Test
    public void list() throws Exception {
        final Set<Path> expected = new HashSet<>(Arrays.asList(
        		Paths.get(LOCAL_DIR.toString()+"/child2/subchild2/src/resources/file2").toAbsolutePath(),
        		Paths.get(LOCAL_DIR.toString()+"/child3/src/resources/file1").toAbsolutePath(),
        		Paths.get(LOCAL_DIR.toString()+"/child4/pom.xml").toAbsolutePath(),
        		Paths.get(LOCAL_DIR.toString()+"/child2/subchild2/src/resources/file22").toAbsolutePath()
        ));
        final Set<Path> actual = Guice.createInjector(new GuiceModule(new ConsoleLogger(), MavenSessionMock.get()))
                        .getInstance(ChangedFiles.class).get().stream().map(p -> p.toAbsolutePath())
                        .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

}
