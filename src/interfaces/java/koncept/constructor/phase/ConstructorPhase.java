package koncept.constructor.phase;

import koncept.constructor.exception.BuildFailedException;
import koncept.constructor.module.Slice;

public interface ConstructorPhase {
	public void perform(Slice slice) throws BuildFailedException;
	
}
