package eu.zalvari.maven.changed.resources.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.StringJoiner;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginUtils {

    private static Logger logger = LoggerFactory.getLogger(PluginUtils.class);

    public static String extractPluginConfigValue(String parameter, Plugin plugin) {
        String value = extractConfigValue(parameter, plugin.getConfiguration());
        for (int i = 0; i < plugin.getExecutions().size() && value == null; i++) {
            value = extractConfigValue(parameter, plugin.getExecutions().get(i).getConfiguration());
        }
        return value;
    }

    private static String extractConfigValue(String parameter, Object configuration) {
        try {
            return ((Xpp3Dom) configuration).getChild(parameter).getValue();
        } catch (Exception ignored) {
        }
        return null;
    }
    
    public static void writeChangedFilesToFile(Collection<Path> projects, File outputFile) {
    	try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)))) {
    		writer.write(joinFilePaths(projects, new StringJoiner("\n")).toString());
    	} catch (IOException e) {
    		logger.warn("Error writing changed projects to file on path :" + outputFile.getPath(), e);
    	}
    }

    public static StringJoiner joinFilePaths(Collection<Path> files, StringJoiner joiner) {
    	for (Path changedFile : files) {
    		joiner.add(changedFile.toString());
    	}
    	return joiner;
    }
    
    public static void copyFiles(Collection<Path> files, Path destination) {
    	   try {
    		   files.forEach(src -> copyFile(src, destination));
    	    } catch (Exception e) {
    	    	logger.error("Failed to copy files ", e.getMessage());
    	        throw new RuntimeException(e.getMessage(), e);
    	    }
    }
    
    public static void copyFile(Path file, Path destination) {
 	   try {
 	        Files.copy(file, destination.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
 	    } catch (Exception e) {
 	    	logger.error("Failed to copy file from: "+file.toString()+" to: "+destination.toString(), e);
 	        throw new RuntimeException(e.getMessage(), e);
 	    }
 }

}
