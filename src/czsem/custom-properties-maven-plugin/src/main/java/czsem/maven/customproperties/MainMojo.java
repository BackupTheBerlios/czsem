package czsem.maven.customproperties;

import java.util.Properties;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @goal setproperties
 */
public class MainMojo extends AbstractMojo {

	/** @parameter default-value="${project}" */
	private MavenProject mavenProject;
	
	@Override
	public void execute() throws MojoExecutionException {
		Properties props = mavenProject.getProperties();
		for (Object objDependency : mavenProject.getDependencies())
		{
			Dependency dependency = (Dependency) objDependency;

			String key = String.format("dependency.%s.%s.version", dependency.getGroupId(),dependency.getArtifactId());
			props.put(key, dependency.getVersion());
			getLog().info("setting artifact version to property: " + key);
		}
	}
}
