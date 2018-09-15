package orchi.HHCloud.AdminService;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Users;

public class ServiceImpl implements Service{
    @Override
    public int suma(int a, int b) {
        return a+b;
    }

    @Override
    public DataUser getUser(String email) {
        DataUser user = null;
        try {
            user = (DataUser) Start.getUserManager().getUserProvider().getUserByEmail(email);
        } catch (UserException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public DataUser editUser(DataUser user) {
        DataUser u = null;
        try {
            u = (DataUser) Start.getUserManager().getUserProvider().editUser(user);
        } catch (UserException e) {
            e.printStackTrace();
        }
        return u;
    }

    @Override
    public DataUser createUser(DataUser user) {
        DataUser u = null;
        try {
            Start.getUserManager().getUserProvider().createUser(user);
            u = getUser(user.getEmail());
        } catch (UserException e) {
            e.printStackTrace();
        }
        return u;
    }

    @Override
    public boolean deleteUser(DataUser user) {
        boolean deleted = false;
        try {
            Start.getUserManager().getUserProvider().deleteUser(user);
            deleted = true;
        } catch (UserException e) {
            e.printStackTrace();
        }
        return deleted;
    }


    @Override
    public Users getAllUsers() {
        Users u = null;
        try {
           u =  Start.getUserManager().getUserProvider().getUsers();
        } catch (UserException e) {
            e.printStackTrace();
        }
        return u;
    }

}
