package orchi.HHCloud.store.arguments;

import java.nio.file.Path;
import java.nio.file.Paths;

import orchi.HHCloud.user.User;

public abstract class Arguments {
    private Path path = Paths.get("/");
    private User user;
    private boolean shareInfo = true;

    public void setShareInfo(boolean shareInfo){
        this.shareInfo = shareInfo;
    }

    public boolean getShareInfo(){
        return shareInfo;
    }

    public String getUserId() {
        return user.getId();
    }

    public User getUse() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
