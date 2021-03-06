package eu.zalvari.maven.changed.resources.mojos;

import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.zalvari.maven.changed.resources.core.ChangedResourceFiles;
import eu.zalvari.maven.changed.resources.core.Configuration;
import eu.zalvari.maven.changed.resources.core.GuiceModule;
import eu.zalvari.maven.changed.resources.core.Property;
import eu.zalvari.maven.changed.resources.utils.MavenToPlexusLogAdapter;

@Mojo(name = "copy", defaultPhase = LifecyclePhase.GENERATE_RESOURCES , threadSafe = true, aggregator = true )
public class CopyChangedResources extends AbstractMojo {
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;

	@Parameter(required = false, property = Property.PREFIX + "enabled", defaultValue = "true")
	public boolean enabled;

	@Parameter(required = false, property = Property.PREFIX + "key", defaultValue = "")
	public String key;

	@Parameter(required = false, property = Property.PREFIX+ "referenceBranch", defaultValue = "refs/remotes/origin/develop")
	public String referenceBranch;

	@Parameter(required = false, property = Property.PREFIX + "baseBranch", defaultValue = "refs/remotes/origin/master")
	public String baseBranch;

	@Parameter(required = false, property = Property.PREFIX + "uncommited", defaultValue = "true")
	public boolean uncommited;

	@Parameter(required = false, property = Property.PREFIX + "compareToMergeBase", defaultValue = "true")
	public boolean compareToMergeBase;

	@Parameter(required = false, property = Property.PREFIX + "fetchBaseBranch", defaultValue = "false")
	public boolean fetchBaseBranch;

	@Parameter(required = false, property = Property.PREFIX + "fetchReferenceBranch", defaultValue = "false")
	public boolean fetchReferenceBranch;

	@Parameter(required = false, property = Property.PREFIX + "outputFile", defaultValue = "${project.basedir}/changed.resources")
	public String outputFile;
	
	@Parameter(required = false, property = Property.PREFIX + "resourcesDir", defaultValue = "")
	public String resourceDir;
	
	@Parameter(required = false, property = Property.PREFIX + "excludeDirs", defaultValue = "")
	public String excludeDirs;
	
	@Parameter(required = false, property = Property.PREFIX + "excludeFiles", defaultValue = "")
	public String excludeFiles;

	@Parameter(required = false, property = Property.PREFIX + "outputDir", defaultValue = "${project.basedir}/changedResources/")
	public String outputDir;
	
	@Parameter(required = false, property = Property.PREFIX + "cleanOutputDir", defaultValue = "true")
	public boolean cleanOutputDir;

	@Parameter(required = false, property = Property.PREFIX + "writeChanged", defaultValue = "false")
	public String writeChanged;

	@Parameter(required = false, property = Property.PREFIX + "useNativeGit", defaultValue = "false")
	public boolean useNativeGit;
	
	@Override	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Injector injector = Guice.createInjector(new GuiceModule(new MavenToPlexusLogAdapter(getLog()), session));
		Configuration configuration = injector.getInstance(Configuration.class);

		ChangedResourceFiles changedResources = injector.getInstance(ChangedResourceFiles.class);
		getLog().info(configuration.toString());

		try {
			changedResources.copy();
		} catch (GitAPIException | IOException e) {
			throw new MojoExecutionException("Exception during Changed Resources Build execution: ", e);
		}
	}

}
