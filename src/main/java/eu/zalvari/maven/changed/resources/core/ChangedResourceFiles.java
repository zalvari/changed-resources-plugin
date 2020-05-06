package eu.zalvari.maven.changed.resources.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	
	public Set<Path> findResources() throws GitAPIException, IOException {
		final Path projectDir = getProjectDir();

		logger.debug("ProjectDir " + projectDir.toString());

		final Path resourcesDir = findResourcesDir(projectDir).get();
		
		final Set<Path> changed = changedFiles.get().stream()
					.filter(p -> p.startsWith(projectDir))
					.filter(p -> p.startsWith(resourcesDir))	
					.collect(Collectors.toSet());
		
		return filterFiles(filterDirs(changed)).stream().filter(Files::exists).collect(Collectors.toSet());
	}
	
	private Set<Path> filterDirs(Set<Path> dirs){
		if (configuration.excludeDirs != null && configuration.excludeDirs.length() > 0) {
			logger.debug("Filter dirs");
			return dirs.stream().filter(f -> Stream.of(configuration.excludeDirs.split(",")).anyMatch(filter -> !f.toString().matches(".*"+filter+".*"))).collect(Collectors.toSet());
		}
		return dirs;
	}
	
	private Set<Path> filterFiles(Set<Path> files){
		if (configuration.excludeFiles != null && configuration.excludeFiles.length() > 0) {
			logger.debug("Filter files");
			return files.stream().filter(f -> Stream.of(configuration.excludeFiles.split(",")).anyMatch(filter -> !f.getFileName().toString().matches(filter))).collect(Collectors.toSet());
		}
		return files;
	}
	
	public void act() throws GitAPIException, IOException {

		final Path targetDir = getTargetDir();
		final Path resourcesDir = findResourcesDir(getProjectDir()).get();
		logger.debug("TargetDir " + targetDir.toString());
		final Set<Path> relChanged = findResources();

		printDelimiter();			
		
		Path outputFilePath = configuration.outputFile.orElse(targetDir.resolve(CHANGED_RESOURCES));
		Path outputDirPath = configuration.outputDir.orElse(targetDir.resolve(CHANGED_RESOURCES_DIR));
		if (configuration.cleanOutputDir) {
			logger.info("Clean output "+outputDirPath);
			Files.walk(outputDirPath)
		      .sorted(Comparator.reverseOrder())
		      .map(Path::toFile)
		      .forEach(File::delete);
			Files.deleteIfExists(outputDirPath);
			Files.deleteIfExists(outputFilePath);
		}
		
		logPaths(relChanged, "Resources to copy:");
		
		if (!relChanged.isEmpty()) {
			writeChangedFiles(relChanged, outputFilePath, getProjectDir().toString());
			copyResourceFiles(relChanged, outputDirPath, resourcesDir);
		}else {
			logger.warn("Skipping... no changed resources found");
		}

		printDelimiter();			
	}
		
	
	private Path getProjectDir() {
		return Paths.get(mavenSession.getCurrentProject().getBasedir().getAbsolutePath());
	}
	
	private Path getTargetDir() {
		return Paths.get(mavenSession.getCurrentProject().getBuild().getDirectory());
	}

	private void writeChangedFiles(Collection<Path> changed, Path outputFile, String baseDir) throws IOException {
		if (configuration.writeChanged) {
			
			if (baseDir != null) {
				PluginUtils.writeChangedFilesToFile(changed.stream()
						.map(p -> p.normalize().toString().replace(baseDir, "").replace("\\", "/").replaceFirst("/", ""))
						.collect(Collectors.toList()), outputFile.toFile());
			}else {
				PluginUtils.writeChangedFilesToFile(changed.stream()
						.map(p -> p.normalize().toString().replace("\\", "/").replaceFirst("/", ""))
						.collect(Collectors.toList()), outputFile.toFile());
			}
		}
	}

	private Optional<Path> findResourcesDir(Path projectDir) {
		if (configuration.resourcesDir != null) {
			logger.debug("Looking for resources dir "+configuration.resourcesDir.get().toString() );
			Path resources = projectDir.resolve(configuration.resourcesDir.get());
			if (Files.isDirectory(resources)) {
				return Optional.of(resources);
			}else {
				logger.debug("Resources dir not found "+configuration.resourcesDir.get().toString() );			
			}
		} 
		logger.debug("Finding a resources dir on "+projectDir.toString() );
		try (Stream<Path> walk = Files.walk(projectDir)) {
			return walk.filter(Files::isDirectory).filter(s -> s.toString().contains("resources")).findFirst();
		} catch (IOException e) {
			logger.error("Error transversing dir", e);
		}
		logger.warn("No resources dir found. Using default: "+projectDir.toString());
		return Optional.of(projectDir);
	}


	private void copyResourceFiles(Collection<Path> changed, Path outputDir, Path basePath) {
		PluginUtils.copyFilesRelativeDir(changed, outputDir, basePath);
	}

	private void logPaths(Collection<Path> projects, String title) {
		logger.info(title);
		logger.info("");
		projects.stream().map(Path::toString).forEach(logger::info);
		logger.info("");
	}

	private void printDelimiter() {
		logger.info("------------------------------------------------------------------------");
	}

}
