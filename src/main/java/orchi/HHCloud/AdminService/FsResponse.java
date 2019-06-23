package orchi.HHCloud.AdminService;

import orchi.HHCloud.store.AbsStatus;
import orchi.HHCloud.store.Status;
import orchi.HHCloud.store.response.Response;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class FsResponse implements Serializable {

    public boolean isFile = false;
    public List<AbsStatus> statues;
    public AbsStatus status = new AbsStatus();


    public static FsResponse fromList(Response status) {
        FsResponse r = new FsResponse();
        r.isFile = status.isFile();


        if (status.isFile()) {
            Status s = (Status) status.getPayload();
            r.status.isFile = s.isFile();
            r.status.path = s.getStringPath();
            r.status.name = s.getName();
            r.status.size = s.getSize();

        }
        if (!status.isFile()) {

            List<Status> payload = (List<Status>) status.getPayload();

            r.statues = payload.stream().map((Status STATUS) -> {
                AbsStatus s = new AbsStatus();
                s.isFile = STATUS.isFile();
                s.path = STATUS.getStringPath();
                s.name = STATUS.getName();
                s.size = STATUS.getSize();
                s.count = Math.toIntExact(STATUS.getElements());

                return s;
            }).collect(Collectors.toList());

        }

        return r;
    }

    public static FsResponse fromStatus(Response status) {
        FsResponse r = new FsResponse();
        r.status.isFile = status.isFile();
        r.status.path = status.getStringPath();

        return r;
    }


    //@Override
    public void writeExternal(ObjectOutput out) throws IOException {
        System.err.println(this);

    }


    // @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }


    @Override
    public String toString() {
        return "FsResponse{" +
                "isFile=" + isFile +
                ", statues=" + statues +
                ", status=" + status +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FsResponse that = (FsResponse) o;
        return isFile == that.isFile &&
                Objects.equals(statues, that.statues) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isFile, statues, status);
    }

}