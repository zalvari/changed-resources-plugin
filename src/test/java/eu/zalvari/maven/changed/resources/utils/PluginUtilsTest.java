package eu.zalvari.maven.changed.resources.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.junit.Test;
import org.slf4j.impl.StaticLoggerBinder;

import eu.zalvari.maven.changed.resources.utils.PluginUtils;

public class PluginUtilsTest {

	public static final Path TEST_WORK_DIR = Paths.get("d:\\").resolve("tmp");
	public static final Path TEST_REPO_DIR = TEST_WORK_DIR.resolve("repo");

	private static Consumer<Path> createdFiles = new Consumer<Path>() {

		@Override
		public void accept(Path t) {
			try {
				if (!Files.exists(t.getParent()))
						Files.createDirectories(t.getParent());
				if (!Files.exists(t))
					Files.createFile(t);
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}

	};

	List<String> strings = Arrays.asList("something", "something");

	StaticLoggerBinder staticLoggerBinder = new StaticLoggerBinder(
			new ConsoleLoggerManager().getLoggerForComponent("Test"));

	@Test
	public void copyFilesTest() {
		final Set<Path> origFiles = new HashSet<>(Arrays.asList(
				Paths.get(TEST_REPO_DIR.toString() + "/child2/subchild2/src/resources/file2").toAbsolutePath(),
				Paths.get(TEST_REPO_DIR.toString() + "/child3/src/resources/file1").toAbsolutePath()));

		origFiles.forEach(createdFiles);

		final Set<String> files = origFiles.stream().map(Path::getFileName).map(Path::toString)
				.collect(Collectors.toSet());
		PluginUtils.copyFiles(origFiles, TEST_WORK_DIR);

		try (Stream<Path> walk = Files.walk(TEST_WORK_DIR)) {
			assertThat(walk.map(Path::getFileName).filter(p -> files.contains(p.toString()))
					.collect(Collectors.counting()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		origFiles.forEach(p -> {
			try {
				Files.deleteIfExists(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	

}