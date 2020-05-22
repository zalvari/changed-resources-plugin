# Changed Resources Plugin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.zalvari/changed-resources-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.zalvari/changed-resources-plugin)
[![Build Status](https://travis-ci.org/zalvari/changed-resources-plugin.svg?branch=master)](https://travis-ci.com/zalvari/changed-resources-plugin)

_**Disclosure** : The code is based on the project [partial-build-plugin](https://github.com/lesfurets/partial-build-plugin)._

A maven plugin to copy changed resources based on changes in the Git repository.

Changed Resources Plugin copies the resources that have been modified in a branch, comparing with a reference branch or tag which can be treated using other resources plugins.
The plugin executes on the Generate Resources phase

## Goals
### copy
Copy the changed files to the configured ``` <outputDir> ```
### list
Write the list of changed files in the configured ``` <outputFile> ```

## Usage

### pom.xml
```xml
    <plugins>
 	<plugin>
        <groupId>io.github.zalvari</groupId>
        <artifactId>changed-resources-plugin</artifactId>
        <version>1.0.2</version>		
        <extensions>true</extensions>	
	<executions>
		<goals>
			<goal>copy</goal>
		</goals>
	</executions>		
	  <configuration>
		  <enabled>true</enabled>
		  <uncommited>false</uncommited>
		  <referenceBranch>refs/heads/master</referenceBranch>
		  <baseBranch>refs/heads/feature1</baseBranch>
		  <outputDir>${project.build.directory}/diff/changedResources</outputDir>
		  <outputFile>${project.build.directory}/diff/resources.changed</outputFile>
		  <resourcesDir>src</resourcesDir>
		  <excludeDirs>proc</excludeDirs>
		  <excludeFiles>file2.sql</excludeFiles>
		</configuration>
      </plugin>
    </plugins>
```
### maven command line

It can be used directly on command line
```
mvn io.github.zalvari:changed-resources-plugin:copy
```
or
```
mvn io.github.zalvari:changed-resources-plugin:list
```

Also you can add the configurations as parameter 
```
mvn io.github.zalvari:changed-resources-plugin:copy -DreferenceBranch=refs/remotes/tag/v1.0 -DexcludeFiles=file1.sql
```

## Configuration

### Configuration parameters

| Parameter                      | Required | Default                               | Description                                                                                                                                                                                                                                              |
|--------------------------------|----------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                        | No       | TRUE                                  | Whether the partial plugin is enabled completely or not                                                                                                                                                                                                  |
| repositorySshKey               | No       | empty                                 | Ssh key used for fetching branches if configured                                                                                                                                                                                                         |
| referenceBranch                | No       | refs/remotes/origin/develop           | 'To' end of branch comparison. Branch name or refspec                                                                                                                                                                                                    |
| baseBranch                     | No       | HEAD                                  | 'From' end of branch comparison. Checked out if different from HEAD                                                                                                                                                                                      |
| uncommited                     | No       | TRUE                                  | Whether to include uncommited changes in branch difference                                                                                                                                                                                               |
| untracked                      | No       | FALSE                                 | Whether to include untracked file changes in branch difference                                                                                                                                                                                           |
| compareToMergeBase             | No       | TRUE                                  | Compare base branch to its merge base with reference branch                                                                                                                                                                                              |
| fetchBaseBranch                | No       | FALSE                                 | Fetch base branch before execution                                                                                                                                                                                                                       |
| fetchReferenceBranch           | No       | FALSE                                 | Fetch reference branch before execution                                                                                                                                                                                                                  |
| outputFile                     | No       | ${project.basedir}/changed.properties | Path of the file to write the changed projects output                                                                                                                                                                                                    |
| outputDir                      | No       | 		                                | Whether to write the files changed                                                                                                                                                                                              |
| excludeDirs                    | No       | empty                                 | Comma separated list of dir names to ignore changed resources                                                                                                                                                                                                     |
| excludeFiles                   | No       | empty                                  | Comma separated list of file names or regex to ignore changed resources                                                                                                                                                                                                     |
| useNativeGit                   | No       | FALSE                                 | Use Native Git commands instead of JGit for detecting changed files. It should also cut down the build bootstrap by a couple of seconds                                                                                                                  |


## Requirements

- Maven version 3+.
