package orchi.HHCloud.store;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Status implements Externalizable {
    private boolean shared = false;
    private Long size = 0L;
    private Long elements = 0L;
    private Long fileCount = 0L;
    private Long directoryCount = 0L;
    private Long spaceQuota = 0L;
    private String name = "";
    @JsonIgnore
    private Path path = Paths.get("/");
    private String mime ="";
    private String permission = "";
    private Long accessTime = 0L;
    private Long modificacionTime = 0L;
    private boolean file = false;
    private Long replication = 0L;


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


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        System.err.println(this);
        out.writeLong(size);
        out.writeLong(elements);
        out.writeLong(fileCount);
        out.writeLong(directoryCount);
        out.writeLong(spaceQuota);
        out.writeBytes(name);
        out.writeBytes(getStringPath());
        out.writeBytes(mime != null? mime:"");
        out.writeBytes(permission);
        out.writeLong(accessTime);
        out.writeLong(modificacionTime);
        out.writeBoolean(file);
        out.writeLong(replication);
    }


    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        size = in.readLong();
        elements = in.readLong();
        fileCount = in.readLong();
        directoryCount = in.readLong();
        spaceQuota = in.readLong();
        name = in.readUTF();
        path = Paths.get(in.readUTF());
        mime = in.readUTF();
        permission = in.readUTF();
        accessTime = in.readLong();
        modificacionTime = in.readLong();
        file = in.readBoolean();
        replication = in.readLong();
    }

    @Override
    public String toString() {
        return "Status{" +
                "shared=" + shared +
                ", size=" + size +
                ", elements=" + elements +
                ", fileCount=" + fileCount +
                ", directoryCount=" + directoryCount +
                ", spaceQuota=" + spaceQuota +
                ", group='" + name + '\'' +
                ", path=" + path +
                ", mime='" + mime + '\'' +
                ", permission='" + permission + '\'' +
                ", accessTime=" + accessTime +
                ", modificacionTime=" + modificacionTime +
                ", file=" + file +
                ", replication=" + replication +
                '}';
    }


}
