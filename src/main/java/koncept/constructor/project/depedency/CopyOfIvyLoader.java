package koncept.constructor.project.depedency;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import koncept.constructor.exception.BuildFailedException;
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

public class CopyOfIvyLoader implements ClasspathDependencyLoader {

	@Override
	public List<String> loadClasspath(Slice slice) throws IOException, BuildFailedException {
		
		List<String> classpath = new ArrayList<>();
		
		
		IvySettings ivySettings = new IvySettings();
		File ivyBaseDir = new File(slice.project().outputDir(), "ivy");
//		ivySettings.setBaseDir(ivyBaseDir);
		ivySettings.setDefaultCache(ivyBaseDir);;

		//ChainResolver ?--> cache, local, central, etc..
		
		IBiblioResolver iBiblioResolver = new IBiblioResolver();
        iBiblioResolver.setM2compatible(true);
        iBiblioResolver.setName("central");
//        for(Object ap: iBiblioResolver.getArtifactPatterns()) {
//        iBiblioResolver pattern:https://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]
//        	System.err.println("iBiblioResolver pattern:" + ap);
//        }
        ivySettings.addResolver(iBiblioResolver);
        
        
//        URLResolver urlResolver = new URLResolver();
//        urlResolver.setM2compatible(true);
//        urlResolver.setName("alternateCentral");
//        urlResolver.addArtifactPattern("http://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[artifact](-[revision]).[ext]");
//        ivySettings.addResolver(urlResolver);
        
        ivySettings.setDefaultResolver(iBiblioResolver.getName());
		
		Ivy ivy = Ivy.newInstance(ivySettings);
		
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
			mainDescriptor.addDependency(depDesc);
		}
		
		try {
			ResolveOptions resolveOptions = new ResolveOptions();
//			resolveOptions.setTransitive(true); //default
//			resolveOptions.setDownload(true); //default
			
			ResolveReport rr = ivy.resolve(mainDescriptor, resolveOptions);
			if (rr.hasError()) {
	            throw new BuildFailedException(rr.getAllProblemMessages().toString());
	        }
			
			ivy.retrieve(
				mainDescriptor.getModuleRevisionId(),
	            ivyBaseDir.getAbsolutePath()+"/[artifact](-[classifier]).[ext]",
	            new RetrieveOptions()
	                // this is from the envelop module
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
