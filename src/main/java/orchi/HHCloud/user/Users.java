package orchi.HHCloud.user;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {
    private List<User> users = new ArrayList<User>();
    @JsonProperty
    private boolean pristine = true;

    public boolean isPristine() {
        return pristine;
    }

    public void setPristine(boolean pristine) {
        this.pristine = pristine;
    }

    public void add(User user) {
        pristine = false;
        if(users.contains(user)){
            users.remove(user);
        }
        users.add(user);
    }

    public void removeUser(User user) {
        pristine = false;
        users.remove(user);
    }

    public List<User> getUsers() {
        return users;
    }

    public void addAll(List<User> users) {
        pristine = false;
        this.users = users;
    }

    public void removeAll() {
        pristine = false;
        users = new ArrayList<User>();
    }

    @Override
    public String toString() {
        return "Users{" +
                "users=" + users +
                '}';
    }
}
