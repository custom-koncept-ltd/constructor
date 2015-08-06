package koncept.constructor.module;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface Slice {

	public Project project();
	
	public String name();
	
	public Set<Slice> sameProjectDependencies();
	
	public File sourceDir(String language) throws IOException;
	
	public File outputDir() throws IOException;
	
}
