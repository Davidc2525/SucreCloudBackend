package orchi.HHCloud.user.userAvailable;

import orchi.HHCloud.user.User;

import java.io.Serializable;

public class AvailableDescriptor implements Serializable{
    private boolean available = false;
    private User user = null;
    private String reason = "";
    private long createdAt = 0L;

    public AvailableDescriptor(){}


    public AvailableDescriptor(User user, String reason,boolean available, long createdAt){
        this.user = user;
        this.reason = reason;
        this.available = available;
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public AvailableDescriptor setUser(User user) {
        this.user = user;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public AvailableDescriptor setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public AvailableDescriptor setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public boolean isAvailable() {
        return available;
    }

    public AvailableDescriptor setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    @Override
    public String toString() {
        return "AvailableDescriptor{" +
                "available=" + available +
                ", user=" + user +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
