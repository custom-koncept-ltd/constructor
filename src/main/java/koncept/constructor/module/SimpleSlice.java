package koncept.constructor.module;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import koncept.constructor.util.DependencyUtils;

public class SimpleSlice implements Slice {
	private final Project project;
	private final String name;
	
	public SimpleSlice(Project project, String name) {
		this.project = project;
		this.name = name;
	}
	
	public Project project() {
		return project;
	}
	
	public String name() {
		return name;
	}
	
	public Set<Slice> sameProjectDependencies() {
		List<String> sliceNames = DependencyUtils.unrollDependencies(name, project.sliceDependencies());
		Map<String, Slice> slices = project.slices();
		LinkedHashSet<Slice> dependencies = new LinkedHashSet<>();
		for(String sliceName: sliceNames) {
			Slice slice = slices.get(sliceName);
			if (slice != null)
				dependencies.add(slice);
		}
		return dependencies;
	}
	
	public File sourceDir(String language) throws IOException {
		File sliceDir = new File(project.sourceDir(), name);
		File languageSourceDir = new File(sliceDir, language);
		return languageSourceDir.getCanonicalFile();
	}
	
	public File outputDir() throws IOException {
		File sliceDir = new File(project.outputDir(), name);
		return sliceDir.getCanonicalFile();
	}
	
}
