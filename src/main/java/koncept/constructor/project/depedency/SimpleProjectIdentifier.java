package koncept.constructor.project.depedency;

import koncept.constructor.exception.ProjectDefinitionException;

public class SimpleProjectIdentifier implements ProjectIdentifier {

	private final String group;
	private final String name;
	private final String version;
	
	public SimpleProjectIdentifier(String group, String name, String version) {
		this.group = group;
		this.name = name;
		this.version = version;
	}
	
	public SimpleProjectIdentifier(String descriptor) throws ProjectDefinitionException {
		String parts[] = descriptor.split(":");
		if (parts.length != 3)
			throw new ProjectDefinitionException("Unable to parse dependency descriptor " + descriptor);
		this.group = parts[0];
		this.name = parts[1];
		this.version = parts[2];
	}
	
	@Override
	public String group() {
		return group;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public String toDependencyString() {
		return group + ":" + name + ":" + version;
	}
}
