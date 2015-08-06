package koncept.constructor.phase;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import koncept.constructor.exception.BuildFailedException;
import koncept.constructor.module.Slice;

public class CleanPhase implements ConstructorPhase {
	private static final Logger logger = Logger.getLogger(CleanPhase.class.getName());

	@Override
	public void perform(Slice slice) throws BuildFailedException {
		try {
			logger.fine("Deleting dir " + slice.project().outputDir().getAbsolutePath());
			delete(slice.project().outputDir());
		} catch (IOException e) {
			throw new BuildFailedException("Unable to clean", e);
		}
	}
	
	private void delete(File file) {
		if (file.isDirectory())
			for(File child: file.listFiles())
				delete(child);
		file.delete();
	}

}
