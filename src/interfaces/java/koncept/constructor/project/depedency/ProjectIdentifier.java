package koncept.constructor.project.depedency;

public interface ProjectIdentifier {

	public String group();
	public String name();
	public String version();
	
	/**
	 * to a three part lookup string
	 * @return
	 */
	public String toDependencyString();
	
	
}
