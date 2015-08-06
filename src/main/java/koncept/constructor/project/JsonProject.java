package koncept.constructor.project;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import koncept.constructor.exception.ProjectDefinitionException;
import koncept.constructor.module.Project;
import koncept.constructor.module.ProjectLoader;
import koncept.constructor.module.SimpleProject;
import koncept.constructor.project.depedency.ProjectDependencies.Type;
import koncept.constructor.project.depedency.SimpleProjectIdentifier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonProject implements ProjectLoader {
	
	@Override
	public Project load(File projectDefinitionFile) throws ProjectDefinitionException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		
		SimpleProject project = new SimpleProject(projectDefinitionFile.getParentFile());
		
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> json = objectMapper.readValue(new FileReader(projectDefinitionFile), Map.class);
			project.identifier(new SimpleProjectIdentifier(
					stringProperty(json, "group", false),
					stringProperty(json, "name", true),
					stringProperty(json, "version", false)));
			
			Map<String, Object> dependencies = mapProperty(json, "dependencies", false);
			for(String dependency: stringListProperty(dependencies, "runtime", false)) {
				project.dependencies().addDependency(dependency, Type.Runtime);
			}
			for(String dependency: stringListProperty(dependencies, "compile", false)) {
				project.dependencies().addDependency(dependency, Type.Compile);
			}
			
			
			project.sliceAutoScan();
		} catch (IOException e) {
			throw new ProjectDefinitionException("Unable to read project", e);
		}	
		
		return project;
	}
	
	private String stringProperty(Map<String, Object> json, String key, boolean required) throws ProjectDefinitionException {
		Object value = get(json, key, required);
		return value == null ? null : value.toString();
	}
	
	private Map<String, Object> mapProperty(Map<String, Object> json, String key, boolean required) throws ProjectDefinitionException {
		Object value = get(json, key, required);
		if (value != null && !Map.class.isAssignableFrom(value.getClass()))
			throw new ProjectDefinitionException("field must be a map: " + key);
		return value == null ? Collections.emptyMap() : (Map)value;
	}
	
	private List<String> stringListProperty(Map<String, Object> json, String key, boolean required) throws ProjectDefinitionException {
		Object value = get(json, key, required);
		if (value != null && String.class.isAssignableFrom(value.getClass()))
			return asList((String)value);
		if (value != null && !List.class.isAssignableFrom(value.getClass()))
			throw new ProjectDefinitionException("field must be a list: " + key);
		return value == null ? Collections.emptyList() : (List)value;
	}
	
	private Object get(Map<String, Object> json, String key, boolean required) throws ProjectDefinitionException {
		if (json == null) {
			if (required) throw new ProjectDefinitionException("unable to fetch field: " + key);
			else return null;
		}
		Object value = json.get(key);
		if (value == null && required)
			throw new ProjectDefinitionException("field must be defined: " + key);
		return value;
	}
}
