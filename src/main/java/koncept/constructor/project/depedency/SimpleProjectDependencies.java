package koncept.constructor.project.depedency;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import koncept.constructor.exception.ProjectDefinitionException;

public class SimpleProjectDependencies implements ProjectDependencies {
	private static final Logger logger = Logger.getLogger(ProjectDependencies.class.getName());
	
	Map<Type, List<ProjectIdentifier>> dependenciesByType = new EnumMap<>(Type.class);
	
	@Override
	public void addDependency(ProjectIdentifier dependency, Type type) {
		logger.finer("adding dependency " + dependency.toDependencyString());
		dependencies(type).add(dependency);
	}

	@Override
	public void addDependency(String dependency, Type type) throws ProjectDefinitionException {
		dependencies(type).add(new SimpleProjectIdentifier(dependency));
	}

	@Override
	public List<ProjectIdentifier> dependencies(Type type) {
		List<ProjectIdentifier> dependencies = dependenciesByType.get(type);
		if (dependencies == null) {
			dependencies = new ArrayList<ProjectIdentifier>();
			dependenciesByType.put(type, dependencies);
		}
		return dependencies;
	}

	
}
