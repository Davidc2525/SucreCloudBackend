package orchi.HHCloud.store;

import java.io.Serializable;
import java.util.Objects;

/**
 * orchi.HHCloud.store.Status abstraido
 * */
public class AbsStatus implements Serializable {
    public boolean isFile = false;
    public String path;
    public String name;
    public Long size;
    public int count;

    @Override
    public String toString() {
        return "AbsStatus{" +
                "isFile=" + isFile +
                ", path='" + path + '\'' +
                ", group='" + name + '\'' +
                ", size=" + size +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbsStatus)) return false;
        AbsStatus absStatus = (AbsStatus) o;
        return isFile == absStatus.isFile &&
                count == absStatus.count &&
                Objects.equals(path, absStatus.path) &&
                Objects.equals(name, absStatus.name) &&
                Objects.equals(size, absStatus.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isFile, path, name, size, count);
    }
}
