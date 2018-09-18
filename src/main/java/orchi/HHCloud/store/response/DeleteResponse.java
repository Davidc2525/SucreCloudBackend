package orchi.HHCloud.store.response;

import java.nio.file.Path;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class DeleteResponse extends Response {
    @JsonIgnore
    private Path parent;

    @JsonProperty(value = "parent")
    public String getStringParent() {
        return parent != null ? parent.toString() : "/";
    }

    public Path getParent() {
        return parent;
    }

    public void setParent(Path parent) {
        this.parent = parent;
    }
}
