package orchi.HHCloud.store.arguments;

import java.nio.file.Path;

public class RenameArguments extends Arguments {
    private Path srcPath;
    private Path dstPath;

    public RenameArguments() {
    }

    public RenameArguments(Path srcPath, Path dstPath) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
    }

    public Path getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(Path srcPath) {
        this.srcPath = srcPath;
    }

    public Path getDstPath() {
        return dstPath;
    }

    public void setDstPath(Path dstPath) {
        this.dstPath = dstPath;
    }


}
