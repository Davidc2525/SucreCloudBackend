package orchi.HHCloud.store.arguments;

import java.nio.file.Path;

public class MkDirArguments extends Arguments{
	private Path path;

	public MkDirArguments() {
	}

	public MkDirArguments(Path path) {
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

}
