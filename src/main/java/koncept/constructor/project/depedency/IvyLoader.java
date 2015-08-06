package koncept.constructor.project.depedency;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import koncept.constructor.exception.BuildFailedException;
import koncept.constructor.module.Project;
import koncept.constructor.module.Slice;
import koncept.constructor.project.depedency.ProjectDependencies.Type;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.IBiblioResolver;

public class IvyLoader implements ClasspathDependencyLoader {

	private Ivy ivy;
	
	public Ivy ivy(Project project) throws IOException {
		if (ivy == null) {
			while(project.parent() != null && project != project.parent()) project = project.parent();
			IvySettings ivySettings = new IvySettings();
			File ivyBaseDir = new File(project.outputDir(), "ivy");
			ivySettings.setBaseDir(ivyBaseDir);
			ivySettings.setDefaultCache(new File(ivyBaseDir, "cache"));
	
			//ChainResolver ?--> cache, local, central, etc..
			
			IBiblioResolver iBiblioResolver = new IBiblioResolver();
	        iBiblioResolver.setM2compatible(true);
	        iBiblioResolver.setName("central");
	        ivySettings.addResolver(iBiblioResolver);
	        ivySettings.setDefaultResolver(iBiblioResolver.getName());
			
			ivy = Ivy.newInstance(ivySettings);
		}
		return ivy;
	}
	
	@Override
	public List<String> loadClasspath(Slice slice) throws IOException, BuildFailedException {
		List<String> classpath = new ArrayList<>();
		
		ProjectIdentifier identifier = slice.project().identifier();
		DefaultModuleDescriptor mainDescriptor = DefaultModuleDescriptor.newDefaultInstance(
				ModuleRevisionId.newInstance(
						identifier.group(),
						identifier.name(),
						identifier.version()));
		
		for(ProjectIdentifier dependencyIdentifier: slice.project().dependencies().dependencies(Type.Compile)) {
			ModuleRevisionId module = ModuleRevisionId.newInstance(
					dependencyIdentifier.group(),
					dependencyIdentifier.name(),
					dependencyIdentifier.version());
			DefaultDependencyDescriptor depDesc = new DefaultDependencyDescriptor(mainDescriptor, module, false, false, true);
			depDesc.addDependencyConfiguration("default", "master");
//			depDesc.addDependencyConfiguration("*", "master");
			mainDescriptor.addDependency(depDesc);
		}
		
		
		
		
		try {
			Ivy ivy = ivy(slice.project());
			
			ResolveReport rr = ivy.resolve(mainDescriptor, new ResolveOptions()
				.setTransitive(true) //default
				.setDownload(true) //default
			);
			if (rr.hasError()) {
	            throw new BuildFailedException(rr.getAllProblemMessages().toString());
	        }
			
			ivy.retrieve(
				mainDescriptor.getModuleRevisionId(),
				ivy.getSettings().getBaseDir().getAbsolutePath() + "/repository/[artifact]-[revision](-[classifier]).[ext]",
	            new RetrieveOptions()
	                .setConfs(new String[]{"default"})
	        );
			
			for(ArtifactDownloadReport artifactReport: rr.getAllArtifactsReports()) {
				classpath.add(artifactReport.getLocalFile().getAbsolutePath());
			}
		} catch (ParseException e) {
			throw new BuildFailedException("Unable to resolve dependencies", e);
		}
		
		return classpath;
		
	}
	
}
