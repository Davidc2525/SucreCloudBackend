package orchi.HHCloud.store.arguments;

import java.nio.file.Path;

public class ListArguments extends Arguments {
    private Path path;

    public ListArguments() {
    }

    public ListArguments(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

}
