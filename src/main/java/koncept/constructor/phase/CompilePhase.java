package koncept.constructor.phase;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import koncept.constructor.exception.BuildFailedException;
import koncept.constructor.module.Slice;

public class CompilePhase implements ConstructorPhase {
	private static final Logger logger = Logger.getLogger(CompilePhase.class.getName());

	@Override
	public void perform(Slice slice) throws BuildFailedException {
		try {
			compile(slice);
		} catch (IOException e) {
			throw new BuildFailedException("Unable to complete build due to I/O exception", e);
		}
		
	}
	
	JavaCompiler lookupCompiler() throws BuildFailedException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler != null)
			return compiler;
		throw new BuildFailedException("Unable to use the compiler. Please install the JDK");
		
	}
	
	public void compile(Slice slice) throws BuildFailedException, IOException {
		logger.fine("compiling slice " + slice.name());
		JavaCompiler compiler = lookupCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".java");
			}
		};
		
		File sourceDir = slice.sourceDir("java");
		File outputDir = slice.outputDir();
		outputDir.mkdirs();
		
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(
				recursiveFileFind(sourceDir, null, true, filter)
				);
		
		
		List<String> options = new ArrayList<>();
		
		options.add("-g");  //include debug info
		
		options.addAll(asList("-d", outputDir.getAbsolutePath())); //output directory
		
		String classpath = classpath(slice);
		System.out.println("classpath = " + classpath);
		options.addAll(asList("-classpath", classpath)); //classpath

		
		SimpleDiagnosticListener diagnosticListener = new SimpleDiagnosticListener();
		
		CompilationTask task = compiler.getTask(
				null, //Writer out
				fileManager, //JavaFileManager  fileManager
				diagnosticListener, //DiagnosticListener diagnosticListener
				options,
				null, //classes
				compilationUnits);

		if (!task.call()) {
			for(String message: diagnosticListener.errorMessages)
				System.out.println(message);
			throw new BuildFailedException("Compilation failed");
		}
	}
	
	public List<String> recursiveFileFind(File dir, String buildPath, boolean absolute, FileFilter filter) throws BuildFailedException {
		if (!dir.isDirectory()) throw new BuildFailedException("Error looking up source files");
		List<String> found = new ArrayList<String>();
		for(File file: dir.listFiles()) {
			String childPath = buildPath == null ?
					file.getName() :
					buildPath + File.separator + file.getName();
			if (file.isDirectory()) {
				found.addAll(recursiveFileFind(file, childPath, absolute, filter));
			} else if (file.isFile()) {
				if (filter.accept(file)) {
					logger.finer("buildPath += " + childPath);
					if (absolute)
						found.add(file.getAbsolutePath());
					else
						found.add(childPath);
				}
			} else throw new BuildFailedException("Unable to locate source files in " + dir.getAbsolutePath());
		}
		return found;
	}
	
	public String classpath(Slice slice) throws IOException, BuildFailedException {
		List<String> classpath = new ArrayList<>();
		
		//first, slice dependencies *within* the project
		Set<Slice> internalDependencies = slice.sameProjectDependencies();
		for(Slice dependency: internalDependencies)
			classpath.add(dependency.outputDir().getAbsolutePath());
		
		
		//then external dependencies
		classpath.addAll(slice.project().dependencyLoader().loadClasspath(slice));
		
		if (classpath.isEmpty())
			return "";
		Iterator<String> it = classpath.iterator();
		StringBuilder sb = new StringBuilder();
		sb.append(it.next());
		while (it.hasNext()) {
			sb.append(File.pathSeparator);
			sb.append(it.next());
		}
		return sb.toString();
	}
	
	
	private static class SimpleDiagnosticListener implements DiagnosticListener {
		public final List<String> errorMessages = new ArrayList<>();
		@Override
		public void report(Diagnostic diagnostic) {
			if (diagnostic.getKind() == Kind.ERROR) {
				errorMessages.add(diagnostic.getMessage(Locale.getDefault()));
			}
//			System.err.println(diagnostic.getKind() + "  " + diagnostic.getMessage(Locale.getDefault()));
		}
	}
	
}
