package koncept.constructor.module;

import java.io.File;

import koncept.constructor.exception.ProjectDefinitionException;

public interface ProjectLoader {

	public Project load(File projectDefinitionFile) throws ProjectDefinitionException;
	
}
