package koncept.constructor.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import koncept.constructor.exception.ProjectDefinitionException;
import koncept.constructor.module.Project;
import koncept.constructor.module.ProjectLoader;
import koncept.constructor.module.SimpleProject;

public class PropertiesProject implements ProjectLoader {

	@Override
	public Project load(File projectDefinitionFile) throws ProjectDefinitionException {
		
		Properties properties = new Properties();
		try (InputStream in = new FileInputStream(projectDefinitionFile)) {
			properties.load(in);
		} catch (IOException e) {
			throw new ProjectDefinitionException("Unable to read project file");
		}
		
		SimpleProject project = new SimpleProject(projectDefinitionFile.getParentFile());
//		project.name(requiredProperty("project.name", properties));
//		project.sliceAutoScan();
		return project;
	}

	
	private String requiredProperty(String key, Properties properties) throws ProjectDefinitionException {
		String value = properties.getProperty(key);
		if (value == null)
			throw new ProjectDefinitionException("");
		return value;
	}
	
}
