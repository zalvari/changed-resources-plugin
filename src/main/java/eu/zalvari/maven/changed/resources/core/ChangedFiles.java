package eu.zalvari.maven.changed.resources.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ChangedFiles {

    @Inject
    private DifferentFiles differentFiles;

    public Set<Path> get() throws GitAPIException, IOException {
        // find changed projects
        return differentFiles.get().stream()
                        .collect(Collectors.toSet());
    }

}
