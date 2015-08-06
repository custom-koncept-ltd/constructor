package koncept.constructor.module;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import koncept.constructor.exception.ProjectDefinitionException;
import koncept.constructor.project.depedency.ClasspathDependencyLoader;
import koncept.constructor.project.depedency.IvyLoader;
import koncept.constructor.project.depedency.ProjectDependencies;
import koncept.constructor.project.depedency.ProjectIdentifier;
import koncept.constructor.project.depedency.SimpleProjectDependencies;
import koncept.constructor.project.depedency.SimpleProjectIdentifier;
import koncept.constructor.util.DependencyUtils;

public class SimpleProject implements Project {
	
	private final SimpleProject parent;
	private ProjectIdentifier identifier;
	private final File projectDirectory;
	private final Map<String, Project> children;
	private final Map<String, Slice> slices;
	private SimpleProjectDependencies dependencies;
	private ClasspathDependencyLoader dependencyLoader;
	
	public SimpleProject(File projectDirectory) {
		this(null, projectDirectory);
	}
	
	public SimpleProject(SimpleProject parent, File projectDirectory) {
		this.parent = parent;
		this.projectDirectory = projectDirectory;
		children = new TreeMap<>();
		slices = new TreeMap<>();
		dependencies = new SimpleProjectDependencies();
	}

	@Override
	public SimpleProject parent() {
		return parent;
	}
	
	
	@Override
	public ProjectIdentifier identifier() {
		return identifier;
	}
	
	public void identifier(ProjectIdentifier identifier) {
		this.identifier = identifier;
	}
	
	public void identifier(String identifier) throws ProjectDefinitionException {
		this.identifier = new SimpleProjectIdentifier(identifier);
	}
	
	
	
	public void registerChild(SimpleProject child) throws ProjectDefinitionException {
		if (child.parent() != this)
			throw new ProjectDefinitionException("Unable to add a child from a different parent project");
		if (children.containsKey(child.identifier().toDependencyString()))
			throw new ProjectDefinitionException("Unable to add a child with a duplicate name");
		children.put(child.identifier().toDependencyString(), child);
	}
	
	@Override
	public Map<String, Project> children() {
		return children;
	}
	
	public void registerSlice(Slice slice) throws ProjectDefinitionException {
		if (slice.project() != this)
			throw new ProjectDefinitionException("Invalid project slice definition");
		if (slices.containsKey(slice.name()))
			throw new ProjectDefinitionException("Invalid project slice definition");
		slices.put(slice.name(), slice);
	}
	
	@Override
	public Map<String, Slice> slices() {
		return slices;
	}
	
	@Override
	public File projectDir() throws IOException {
		return projectDirectory.getCanonicalFile();
	}
	
	@Override
	public File sourceDir() throws IOException {
		return new File(projectDirectory, "src").getCanonicalFile();
	}
	
	@Override
	public File outputDir() throws IOException  {
		return new File(projectDirectory, "constructed").getCanonicalFile();
	}
	
	@Override
	public List<String> buildTargets(String target) {
		return DependencyUtils.unrollDependencies(target, Project.Defaults.defaultTargetDependencies);
	}
	
	@Override
	public ProjectDependencies dependencies() {
		return dependencies;
	}
	
	@Override
	public Map<String, List<String>> sliceDependencies() {
		//if (overridden) useOverrides - can be 'overridden' to default
		if (parent != null)
			return parent.sliceDependencies();
		return Project.Defaults.defaultSliceDependencies;
	}
	
	@Override
	public ClasspathDependencyLoader dependencyLoader() {
		if (dependencyLoader != null)
			return dependencyLoader;
		if (parent != null)
			return parent.dependencyLoader();
		dependencyLoader = new IvyLoader();
		return dependencyLoader;
	}

	public void dependencyLoader(ClasspathDependencyLoader dependencyLoader) {
		this.dependencyLoader = dependencyLoader;
	}
	
	public void sliceAutoScan() throws ProjectDefinitionException, IOException {
		for(File sliceSourceDirectory: sourceDir().listFiles())
			registerSlice(new SimpleSlice(this, sliceSourceDirectory.getName()));
	}
}
