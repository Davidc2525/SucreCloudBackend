package orchi.HHCloud.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {
    private List<DataUser> users = new ArrayList<DataUser>();

    public void add(DataUser user) {
        users.add(user);
    }

    public void removeUser(DataUser user) {
        users.remove(user);
    }

    public List<DataUser> getUsers() {
        return users;
    }
}
