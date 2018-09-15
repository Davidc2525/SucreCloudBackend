package orchi.HHCloud.AdminService;

import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Users;

public interface Service {
    public int suma(int a, int b);

    public DataUser getUser(String email);

    public DataUser editUser(DataUser user);

    public DataUser createUser(DataUser newUser);

    public boolean deleteUser(DataUser user);

    public Users getAllUsers();
}
