package koncept.constructor.module;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import koncept.constructor.project.depedency.ClasspathDependencyLoader;
import koncept.constructor.project.depedency.ProjectDependencies;
import koncept.constructor.project.depedency.ProjectIdentifier;

public interface Project {
	
	public static final class Defaults {
		public static final Map<String, List<String>> defaultSliceDependencies;
		public static final Map<String, List<String>> defaultTargetDependencies;
		static {
			Map<String, List<String>> map = new HashMap<>();
			map.put("test", asList("main"));
			map.put("main", asList("interfaces"));
			defaultSliceDependencies = Collections.unmodifiableMap(map);
			
			map = new HashMap<>();
			map.put("jar", asList("test"));
			map.put("deploy", asList("test"));
			map.put("test", asList("compile"));
			defaultTargetDependencies = Collections.unmodifiableMap(map);
			
		}
	}

	/**
	 * The parent project, or null if this project has no parent
	 * @return
	 */
	public Project parent();
	
	/**
	 * The project identifier.<br>
	 * Note that the group and artifiact must be unique
	 * @return
	 */
	public ProjectIdentifier identifier();
	
	/**
	 * A Map of this projects children, by child name
	 * @return
	 */
	public Map<String, Project> children();
	
	/**
	 * A list of all internal project slices
	 * @return
	 */
	public Map<String, Slice> slices();
	
	public ProjectDependencies dependencies();
	
	public ClasspathDependencyLoader dependencyLoader();
	
	public File projectDir() throws IOException;
	
	public File sourceDir() throws IOException;
	
	public File outputDir() throws IOException;
	
	public List<String> buildTargets(String target);
	
	public Map<String, List<String>> sliceDependencies();
	
}
