package orchi.HHCloud.AdminService;

import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.quota.Quota;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Users;
import orchi.HHCloud.user.userAvailable.AvailableDescriptor;
import orchi.HHCloud.user.userAvailable.Exceptions.DisablingException;
import orchi.HHCloud.user.userAvailable.Exceptions.EnablingException;

public interface Service {

    public int suma(int a, int b);

    /**
     * Servicio de usuario
     */
    public DataUser getUser(String email) throws UserException;

    public DataUser editUser(DataUser user) throws UserException, QuotaException;

    public DataUser createUser(DataUser newUser) throws UserException;

    public boolean deleteUser(DataUser user) throws UserException, QuotaException;

    public Users getAllUsers();

    /**Disponibilidad de usuario*/
    public AvailableDescriptor disableUser(DataUser user, String reason) throws DisablingException, UserException;

    public AvailableDescriptor enableUser(DataUser user) throws EnablingException, UserException;

    public AvailableDescriptor getAvialableDescriptor(DataUser user) throws UserException;

    /**
     * Servicio de detalles almacenamiento
     */
    public ContentSummary getContentSummary(DataUser user);

    public Quota setQuota(DataUser user, Quota q) throws QuotaException;
}