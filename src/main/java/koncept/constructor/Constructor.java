package koncept.constructor;

import static koncept.constructor.util.DependencyUtils.orderDependencies;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import koncept.constructor.exception.BuildFailedException;
import koncept.constructor.exception.ProjectDefinitionException;
import koncept.constructor.module.Project;
import koncept.constructor.module.ProjectLoader;
import koncept.constructor.module.Slice;
import koncept.constructor.phase.ConstructorPhase;

public class Constructor {
	private static final Logger logger = Logger.getLogger(Constructor.class.getName());
	private static final String buildFilePrefix = "constructor.";
	private static final String[] defaultTargets = {"clean", "compile"};
	
	File buildFile = null;
	String buildFileType = null;
	String projectLoaderClass = null;
	
	private PrintStream out;
	
	public static void main(String[] args) throws Exception {
		Constructor c = new Constructor();
		
		String buildFile = System.getProperty("buildFile");
		if (buildFile != null)
			c.buildFile = new File(new File("."), buildFile);
		
		c.buildFileType = System.getProperty("buildFileType");
		c.projectLoaderClass = System.getProperty("projectLoaderClass");
		
		if (args.length == 0)
			c.run(defaultTargets);
		else
			c.run(args);
	}
	
	public Constructor() {
		out = System.out;
	}
	
	public void run(String... targets) throws ProjectDefinitionException, BuildFailedException {
		File cd = new File(".");
		logger.finer("currend working directory = " + cd.getAbsolutePath());
		
		
		if (buildFile == null) {
			File[] buildFiles = cd.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(buildFilePrefix);
				}
			});
			if (buildFiles.length == 0) {
				throw new ProjectDefinitionException("No project files to load.");
			} else if (buildFiles.length != 1) {
				StringBuilder fileNames = new StringBuilder();
				for(File file: buildFiles) {
					fileNames.append(" ");
					fileNames.append(file.getName());
				}
				throw new ProjectDefinitionException("Multiple project files available:" + fileNames.toString());
			}
			buildFile = buildFiles[0];
		}
		if (buildFileType == null)
			buildFileType = 
					buildFile.getName().substring(buildFilePrefix.length(), buildFilePrefix.length() + 1).toUpperCase() +
					buildFile.getName().substring(buildFilePrefix.length() + 1);
		logger.fine("buildFileType = " + buildFileType);
		
		if (projectLoaderClass == null)
			projectLoaderClass = "koncept.constructor.project." + buildFileType + "Project";
		logger.fine("projectLoaderClass = " + projectLoaderClass);
		
		Project project = null;
		try {
			ProjectLoader projectLoader = (ProjectLoader)Class.forName(projectLoaderClass).newInstance();
			project = projectLoader.load(buildFile);
		} catch (IllegalAccessException e) {
			throw new ProjectDefinitionException("Unable to create loader for type " + buildFileType);
		} catch (ClassNotFoundException e) {
			throw new ProjectDefinitionException("Unable to find loader for type " + buildFileType);
		} catch (InstantiationException e) {
			throw new ProjectDefinitionException("Unable to create loader for type " + buildFileType);
		}
		
		if (project == null)
			throw new ProjectDefinitionException("Unable to create project");
		
		build(project, targets);
		
	}
	
	
	public void build(Project project, String... targets) throws ProjectDefinitionException, BuildFailedException {
		for(String target: targets) {
			out.println("Constructing project " + project.identifier().name() + ": " + target);
			List<String> buildOrder = project.buildTargets(target);
			for(String execTarget: buildOrder)
				runPhase(project, execTarget);
		}
		out.println("Construction complete");
	}
	
	
	public void runPhase(Project project, String target) throws ProjectDefinitionException, BuildFailedException {
		String constructorClass = "koncept.constructor.phase." + target.substring(0, 1).toUpperCase() + target.substring(1) + "Phase";
		logger.fine("constructorClass = " + constructorClass);
		
		try {
			
			out.println("Construction phase " + target + " commencing");
			
			Map<String, Slice> slices = project.slices();
			List<String> sliceOrder = orderDependencies(slices.keySet(), project.sliceDependencies());
			
			ConstructorPhase phase = (ConstructorPhase)Class.forName(constructorClass).newInstance();
			
			for(String sliceName: sliceOrder) {
				Slice slice = slices.get(sliceName);
				if (slice != null) { //default slice names *may* be missed
					out.println(project.identifier().name() + " >" + target + ":" + sliceName);
					phase.perform(slice);
				}
			}
		} catch (IllegalAccessException e) {
			throw new ProjectDefinitionException("Unable to create constructor for type " + target);
		} catch (ClassNotFoundException e) {
			throw new ProjectDefinitionException("Unable to find constructor for type " + target);
		} catch (InstantiationException e) {
			throw new ProjectDefinitionException("Unable to create constructor for type " + target);
		}
		
	}

	
}
