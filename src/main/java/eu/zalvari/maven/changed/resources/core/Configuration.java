package eu.zalvari.maven.changed.resources.core;

import static eu.zalvari.maven.changed.resources.utils.PluginUtils.extractPluginConfigValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Configuration {

    private static final String PLUGIN_KEY = "eu.zalvari:changed-resources-plugin";

    public final boolean enabled;
    public final Path key;
    public final String referenceBranch;
    public final String baseBranch;
    public final boolean uncommited;
    public final boolean untracked;
    public final boolean compareToMergeBase;
    public final boolean fetchBaseBranch;
    public final boolean fetchReferenceBranch;
    public final Optional<Path> outputFile;
    public final Optional<Path> outputDir;
    public final boolean writeChanged;
    public final boolean useNativeGit;
    public final String rootDirectory;
    public final Optional<Path> resourcesDir;
    public final String excludeDirs;
    public final String excludeFiles;
    

    @Inject
    public Configuration(MavenSession session) throws IOException {

        try {
            Plugin plugin = session.getCurrentProject().getPlugin(PLUGIN_KEY);
            // check properties
            checkPluginConfiguration(plugin);
            checkProperties(session.getCurrentProject().getProperties());
            checkProperties(System.getProperties());
            checkProperties(session.getUserProperties());
            // parse into configuration
            enabled = Boolean.valueOf(Property.enabled.getValue());
            key = parseKey(session, Property.repositorySshKey.getValue());
            referenceBranch = Property.referenceBranch.getValue();
            baseBranch = Property.baseBranch.getValue();
            uncommited = Boolean.valueOf(Property.uncommited.getValue());
            untracked = Boolean.valueOf(Property.untracked.getValue());
            compareToMergeBase = Boolean.valueOf(Property.compareToMergeBase.getValue());
            fetchReferenceBranch = Boolean.valueOf(Property.fetchReferenceBranch.getValue());
            fetchBaseBranch = Boolean.valueOf(Property.fetchBaseBranch.getValue());
            outputFile = parseFilePath(session, Property.outputFile.getValue());
            outputDir = parseFilePath(session, Property.outputDir.getValue());
            writeChanged = Boolean.valueOf(Property.writeChanged.getValue());
            useNativeGit = Boolean.valueOf(Property.useNativeGit.getValue());
            rootDirectory = session.getExecutionRootDirectory();
            resourcesDir = parseFilePath(session, Property.resourcesDir.getValue());
            excludeDirs = Property.excludeDirs.getValue();
            excludeFiles =  Property.excludeFiles.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Path parseKey(MavenSession session, String keyOptionValue) throws IOException {
        Path pomDir = session.getCurrentProject().getBasedir().toPath();
        if (keyOptionValue != null && !keyOptionValue.isEmpty()) {
            return pomDir.resolve(keyOptionValue).toAbsolutePath().toRealPath().normalize();
        }
        return null;
    }

    private Optional<Path> parseFilePath(MavenSession session, String outputFileValue) throws IOException {
        Path pomDir = session.getCurrentProject().getBasedir().toPath();
        if (outputFileValue != null && !outputFileValue.isEmpty()) {
            return Optional.of(pomDir.resolve(outputFileValue).toAbsolutePath().normalize());
        }
        return Optional.empty();
    }
    

    private void checkPluginConfiguration(Plugin plugin) {
        if (null != plugin) {
            Arrays.stream(Property.values())
                            .forEach(p -> p.setValue(extractPluginConfigValue(p.name(), plugin)));
        }
    }

    private void checkProperties(Properties properties) throws MavenExecutionException {
        try {
            properties.stringPropertyNames().stream()
                            .filter(s -> s.startsWith(Property.PREFIX))
                            .map(s -> s.replaceFirst(Property.PREFIX, ""))
                            .map(Property::valueOf)
                            .forEach(p -> p.setValue(properties.getProperty(p.fullName())));
        } catch (IllegalArgumentException e) {
            throw new MavenExecutionException("Invalid invalid GIB property found. Allowed properties: \n"
                            + Property.exemplifyAll(), e);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                        .append("enable", enabled)
                        .append("key", key)
                        .append("referenceBranch", referenceBranch)
                        .append("baseBranch", baseBranch)
                        .append("uncommited", uncommited)
                        .append("untracked", untracked)
                        .append("compareToMergeBase", compareToMergeBase)
                        .append("fetchBaseBranch", fetchBaseBranch)
                        .append("fetchReferenceBranch", fetchReferenceBranch)
                        .append("outputFile", outputFile)
                        .append("outputDir", outputDir)
                        .append("writeChanged", writeChanged)
                        .append("useNativeGit", useNativeGit)
                        .append("resourcesDir", resourcesDir)
                        .append("excludeDirs", excludeDirs)
                        .append("excludeFiles", excludeFiles)
                        .toString();
    }
}
