package orchi.HHCloud.AdminService;

import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Users;

public interface Service {
    public int suma(int a, int b);

    /**
     * Servicio de usuario*/
    public DataUser getUser(String email);

    public DataUser editUser(DataUser user);

    public DataUser createUser(DataUser newUser);

    public boolean deleteUser(DataUser user);

    public Users getAllUsers();

    /**Servicio de detalles almacenamiento*/
    public ContentSummary getContentSummary(DataUser user);

}