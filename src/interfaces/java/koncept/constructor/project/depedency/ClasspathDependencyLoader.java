package koncept.constructor.project.depedency;

import java.io.IOException;
import java.util.List;

import koncept.constructor.exception.BuildFailedException;
import koncept.constructor.module.Slice;

public interface ClasspathDependencyLoader {

	public List<String> loadClasspath(Slice slice) throws IOException, BuildFailedException;
	
}
