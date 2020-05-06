package eu.zalvari.maven.changed.resources.mojos;

import java.io.IOException;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.zalvari.maven.changed.resources.core.ChangedResourceFiles;
import eu.zalvari.maven.changed.resources.core.Configuration;
import eu.zalvari.maven.changed.resources.core.GuiceModule;

@Component( role = AbstractMavenLifecycleParticipant.class)
public class ChangedResourcesLifeCycle extends AbstractMavenLifecycleParticipant {
	
    @Requirement
    private Logger logger;
   
	@Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		final Injector injector = Guice.createInjector(new GuiceModule(logger, session));
		Configuration configuration = injector.getInstance(Configuration.class);

		ChangedResourceFiles changedResources = injector.getInstance(ChangedResourceFiles.class);
		logger.info(configuration.toString());

		try {

			if (configuration.enabled) {
				logger.info("Changed resources plugin enabled...");
				changedResources.act();
		      } else {
	                logger.info("Changed resources plugin disabled...");
	            }
		} catch (GitAPIException | IOException e) {
			 throw new MavenExecutionException("Exception during Change resources Build execution: " + e.getMessage(), e);
		}
	}
	
}
