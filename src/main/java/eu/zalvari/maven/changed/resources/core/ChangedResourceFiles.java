package eu.zalvari.maven.changed.resources.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import eu.zalvari.maven.changed.resources.utils.PluginUtils;

@Singleton
public class ChangedResourceFiles {

	public static final String CHANGED_RESOURCES = "changed.resources";
	public static final String CHANGED_RESOURCES_DIR = "changedResources";

	@Inject
	private Configuration configuration;
	@Inject
	private Logger logger;
	@Inject
	private ChangedFiles changedFiles;
	@Inject
	private MavenSession mavenSession;

	public void act() throws GitAPIException, IOException {

		final Set<Path> changed = changedFiles.get();
		if (!changed.isEmpty()) {
			printDelimiter();
			logProjects(changed, "Changed Resources:");
		}
		writeChangedFiles(changed);
		copyResourceFiles(changed);
	}

	private void writeChangedFiles(Collection<Path> changed) {
		if (configuration.writeChanged) {
			Path outputFilePath = configuration.outputFile.orElse(mavenSession.getCurrentProject().getBasedir().toPath());
			PluginUtils.writeChangedFilesToFile(changed, outputFilePath.toFile());
		}
	}
	
	private void copyResourceFiles(Collection<Path> changed) {
		Path outputFilePath = configuration.outputDir.orElse(mavenSession.getCurrentProject().getBasedir().toPath());
		PluginUtils.copyFiles(changed, outputFilePath);		
	}
	

	private void logProjects(Collection<Path> projects, String title) {
		logger.info(title);
		logger.info("");
		projects.stream().map(Path::toString).forEach(logger::info);
		logger.info("");
	}

	private void printDelimiter() {
		logger.info("------------------------------------------------------------------------");
	}

}
