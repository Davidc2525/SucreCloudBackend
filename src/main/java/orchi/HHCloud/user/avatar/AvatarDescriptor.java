package orchi.HHCloud.user.avatar;

import java.io.Serializable;

public class AvatarDescriptor implements Serializable {

    private String uid;
    private String hash;
    private long lastModified;
    private boolean hashAvatar = false;

    public AvatarDescriptor() {
    }

    public AvatarDescriptor(String uid, String hash, long lastModified, boolean hashAvatar) {
        this.uid = uid;
        this.hash = hash;
        this.lastModified = lastModified;
        this.hashAvatar = hashAvatar;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isHashAvatar() {
        return hashAvatar;
    }

    public void setHashAvatar(boolean hashAvatar) {
        this.hashAvatar = hashAvatar;
    }

    @Override
    public String toString() {
        return "AvatarDescriptor{" +
                "uid='" + uid + '\'' +
                ", hash='" + hash + '\'' +
                ", lastModified=" + lastModified +
                ", hashAvatar=" + hashAvatar +
                '}';
    }
}
