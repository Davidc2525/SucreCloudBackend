package orchi.HHCloud.store.arguments;

import java.nio.file.Path;
import java.util.List;

import javax.servlet.AsyncContext;

public class DownloadArguments extends Arguments {

	private AsyncContext ctx;
	private Path path;
	private List<Path> paths;

	public DownloadArguments() {
	}

	public DownloadArguments(AsyncContext ctx, Path path, List<Path> paths) {
		this.ctx = ctx;
		this.path = path;
		this.paths = paths;
	}

	public AsyncContext getCtx() {
		return ctx;
	}

	public void setCtx(AsyncContext ctx) {
		this.ctx = ctx;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
	}
}
