package koncept.constructor.project.depedency;

import java.util.List;

import koncept.constructor.exception.ProjectDefinitionException;

public interface ProjectDependencies {

	public static enum Type{Compile, Runtime}
	
	public void addDependency(ProjectIdentifier dependency, Type type) throws ProjectDefinitionException;
	public void addDependency(String dependency, Type type) throws ProjectDefinitionException;
	
	/**
	 * return the dependencies for that type.<br>
	 * Runtime dependencies are NOT automatically added to the Compile time dependencies. but are included at runtime nonetheless.
	 * @param type
	 * @return
	 */
	public List<ProjectIdentifier> dependencies(Type type);
	
}
