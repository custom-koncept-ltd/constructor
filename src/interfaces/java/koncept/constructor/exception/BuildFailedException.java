package koncept.constructor.exception;

public class BuildFailedException extends Exception {

	public BuildFailedException(String message) {
		super(message);
	}
	
	public BuildFailedException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
