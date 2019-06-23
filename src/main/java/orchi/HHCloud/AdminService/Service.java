package orchi.HHCloud.AdminService;

import orchi.HHCloud.auth.Exceptions.AuthException;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.quota.Quota;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Users;
import orchi.HHCloud.user.userAvailable.AvailableDescriptor;
import orchi.HHCloud.user.userAvailable.Exceptions.DisablingException;
import orchi.HHCloud.user.userAvailable.Exceptions.EnablingException;

import java.util.List;

/**
 * @author david
 */
public interface Service {

    int suma(int a, int b);

    /*login*/
    DataUser singIn(DataUser user) throws AuthException, UserException;

    /*
     * Servicio de usuario
     */
    DataUser getUser(String email) throws UserException;

    DataUser editUser(DataUser user) throws UserException, QuotaException;

    DataUser createUser(DataUser newUser) throws UserException;

    boolean deleteUser(DataUser user) throws UserException, QuotaException;

    Users getAllUsers();

    Users search(String query);

    /*Disponibilidad de usuario*/
    AvailableDescriptor disableUser(DataUser user, String reason) throws DisablingException, UserException;

    AvailableDescriptor enableUser(DataUser user) throws EnablingException, UserException;

    AvailableDescriptor getAvialableDescriptor(DataUser user) throws UserException;

    /*
     * Servicio de detalles almacenamiento
     */
    ContentSummary getContentSummary(DataUser user);

    Quota setQuota(DataUser user, Quota q) throws QuotaException;

    FsResponse listPath(DataUser user, String path);

    FsResponse statusPath(DataUser user, String path);

    List<String> removePaths(DataUser user, List<String> paths);

    String deletePath(DataUser user, String path);

    boolean copyToLocal(DataUser user, String srcPath, String dstPath) throws Exception;
}