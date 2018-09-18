package orchi.HHCloud.store.arguments;

import java.nio.file.Path;
import java.util.List;

public class MoveOrCopyArguments extends Arguments {
    private boolean move;
    private Path srcPath;
    private Path dstPath;
    private List<Path> srcPaths;

    public MoveOrCopyArguments() {
    }

    public MoveOrCopyArguments(Path dstPath, List<Path> srcPaths) {
        this.dstPath = dstPath;
        this.srcPaths = srcPaths;
    }

    public MoveOrCopyArguments(Path srcPath, Path dstPath) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
    }

    public MoveOrCopyArguments(boolean move, Path dstPath, List<Path> srcPaths) {
        this.move = move;
        this.dstPath = dstPath;
        this.srcPaths = srcPaths;
    }

    public MoveOrCopyArguments(boolean move, Path srcPath, Path dstPath) {
        this.move = move;
        this.srcPath = srcPath;
        this.dstPath = dstPath;
    }

    public MoveOrCopyArguments(boolean move, Path srcPath, Path dstPath, List<Path> srcPaths) {
        this.move = move;
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.srcPaths = srcPaths;
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

    public List<Path> getSrcPaths() {
        return srcPaths;
    }

    public void setSrcPaths(List<Path> srcPaths) {
        this.srcPaths = srcPaths;
    }

    public boolean isMultiple() {
        return srcPaths != null;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }
}
