package orchi.HHCloud.provider;

public enum Groups {
    GLOBAL("ochi.hhcloud.provider.GLOBAL"),
    USER("ochi.hhcloud.provider.USER"),
    STORE("ochi.hhcloud.provider.STORE"),;

    Groups(String s) {
        group = s;
    }

    public final String group;


    public boolean equals(String otherName) {
        return group.equals(otherName);
    }

    public String toString() {
        return this.group;
    }


}
