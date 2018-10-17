package orchi.HHCloud.user.search;

import orchi.HHCloud.user.User;

import java.util.ArrayList;
import java.util.List;

public class UsersFound {

    private List<User> founds = new ArrayList<User>();


    public User add(User user){
        founds.add(user);
        return user;
    }

    public List<User> getAll(){
        return founds;
    }

    @Override
    public String toString() {
        return "UsersFound{" +
                "founds=" + founds +
                '}';
    }
}


