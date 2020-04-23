package eu.zalvari.maven.changed.resources.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface DifferentFiles {

    Set<Path> get() throws IOException;
}
