package orchi.HHCloud.store;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.nio.file.Path;

public class Status {
    private boolean shared = false;
    private Long size;
    private Long elements;
    private Long fileCount;
    private Long directoryCount;
    private Long spaceQuota;
    private String name;
    @JsonIgnore
    private Path path;
    private String mime;
    private String permission;
    private Long accessTime;
    private Long modificacionTime;
    private boolean file;
    private Long replication;

    public Status() {
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getElements() {
        return elements;
    }

    public void setElements(Long elements) {
        this.elements = elements;
    }

    public Long getFileCount() {
        return fileCount;
    }

    public void setFileCount(Long fileCount) {
        this.fileCount = fileCount;
    }

    public Long getDirectoryCount() {
        return directoryCount;
    }

    public void setDirectoryCount(Long directoryCount) {
        this.directoryCount = directoryCount;
    }

    public Long getSpaceQuota() {
        return spaceQuota;
    }

    public void setSpaceQuota(Long spaceQuota) {
        this.spaceQuota = spaceQuota;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(value = "path")
    public String getStringPath() {
        return path.toString();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(Long accessTime) {
        this.accessTime = accessTime;
    }

    public Long getModificacionTime() {
        return modificacionTime;
    }

    public void setModificacionTime(Long modificacionTime) {
        this.modificacionTime = modificacionTime;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public Long getReplication() {
        return replication;
    }

    public void setReplication(Long replication) {
        this.replication = replication;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

}
