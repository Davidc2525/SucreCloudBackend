package orchi.HHCloud.store.arguments;

import java.nio.file.Path;
import java.util.List;

public class DeleteArguments extends Arguments {
    private Path path;
    private List<Path> paths;

    public DeleteArguments() {
    }

    public DeleteArguments(Path path) {
        this.path = path;
    }

    public DeleteArguments(Path path, List<Path> paths) {
        this.path = path;
        this.paths = paths;
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

    public boolean isMultiple() {
        return paths != null;
    }

}
