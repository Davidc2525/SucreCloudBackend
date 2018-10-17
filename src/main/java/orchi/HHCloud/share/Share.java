/**
 * Share.java
 */
package orchi.HHCloud.share;

import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Users;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * @author david 14 ago. 2018
 */
public class Share implements Serializable {
    private String id;
    private User owner;
    private long sharedAt;
    private Mode mode;
    @JsonIgnore
    private Path path; // ruta absoluta, completa
    private Users shareWith;

    @JsonProperty(value = "path")
    public String getStringPath() {
        return path != null ? path.toString() == null ? "/" : path.toString() : "/";
    }

    public Users getShareWith() {
        return shareWith;
    }

    public void setShareWith(Users shareWith) {
        this.shareWith = shareWith;
    }

    public long getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(long sharedAt) {
        this.sharedAt = sharedAt;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "Share{" +
                "id='" + id + '\'' +
                ", owner=" + owner +
                ", sharedAt=" + sharedAt +
                ", mode=" + mode +
                ", path=" + path +
                ", shareWith=" + shareWith +
                '}';
    }
}
