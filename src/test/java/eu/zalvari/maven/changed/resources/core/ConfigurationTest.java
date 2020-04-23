package eu.zalvari.maven.changed.resources.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.junit.Test;

import eu.zalvari.maven.changed.resources.core.Configuration;
import eu.zalvari.maven.changed.resources.core.Property;
import eu.zalvari.maven.changed.resources.mocks.MavenSessionMock;
import eu.zalvari.maven.changed.resources.mocks.ModuleMock;

public class ConfigurationTest {

    @Test
    public void exemplifyAll() {
        System.out.println(Property.exemplifyAll());
    }

    @Test
    public void userProperties() throws Exception {
        MavenSession mavenSession = MavenSessionMock.get();
        Properties properties = new Properties();
        properties.setProperty(Property.referenceBranch.fullName(), "refs/test/branch");
        when(mavenSession.getUserProperties()).thenReturn(properties);
        ModuleMock module = ModuleMock.module(mavenSession);
        Configuration arguments = module.arguments();
        assertEquals("refs/test/branch", arguments.referenceBranch);
    }

    @Test(expected = Exception.class)
    public void badProperty() throws Exception {
        MavenSession mavenSession = MavenSessionMock.get();
        Properties properties = new Properties();
        properties.setProperty("changedResources.badProperty", "refs/test/branch");
        when(mavenSession.getUserProperties()).thenReturn(properties);
        ModuleMock module = ModuleMock.module(mavenSession);
        Configuration arguments = module.arguments();
    }

}
