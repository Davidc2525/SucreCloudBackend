package orchi.HHCloud.AdminService;

import orchi.HHCloud.Start;
import orchi.HHCloud.auth.AuthProvider;
import orchi.HHCloud.auth.Exceptions.AuthException;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.quota.Quota;
import orchi.HHCloud.quota.QuotaProvider;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.store.ContextStore;
import orchi.HHCloud.store.StoreManager;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.RoledUser;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.Users;
import orchi.HHCloud.user.role.Roles;
import orchi.HHCloud.user.search.SearchUserProvider;
import orchi.HHCloud.user.search.UsersFound;
import orchi.HHCloud.user.userAvailable.AvailableDescriptor;
import orchi.HHCloud.user.userAvailable.Exceptions.DisablingException;
import orchi.HHCloud.user.userAvailable.Exceptions.EnablingException;
import orchi.HHCloud.user.userAvailable.UserAvailableProvider;

import java.nio.file.Paths;

public class ServiceImpl implements Service {
    private QuotaProvider qp = Start.getQuotaManager().getProvider();
    private StoreProvider sp = Start.getStoreManager().getStoreProvider();
    private UserAvailableProvider avp = Start.getUserManager().getUserAvailableProvider();
    private UserProvider up = Start.getUserManager().getUserProvider();
    private AuthProvider ap = Start.getAuthProvider();
    private SearchUserProvider usp = Start.getUserManager().getSearchUserProvider();

    @Override
    public int suma(int a, int b) {
        return a + b;
    }

    @Override
    public DataUser singIn(DataUser user) throws AuthException, UserException {
        DataUser u = (DataUser) up.getUserByEmail(user.getEmail());
        if(Roles.ADMIN == RoledUser.byUser(u).getRole().getRole()){
            ap.authenticate(user,authUser -> {});
        }else{
            throw new AuthException(String.format("El usuario %s no es un administrador.",u.getEmail()));
        }

        return user;
    }

    @Override
    public DataUser getUser(String email) throws UserException {
        DataUser user = null;
        user = (DataUser) Start.getUserManager().getUserProvider().getUserByEmail(email);

        return user;
    }

    @Override
    public DataUser editUser(DataUser user) throws UserException, QuotaException {
        DataUser u = null;
        DataUser oldU = null;
        oldU = (DataUser) Start.getUserManager().getUserProvider().getUserById(user.getId());
        u = (DataUser) Start.getUserManager().getUserProvider().editUser(user);
        if ((oldU.isEmailVerified() != u.isEmailVerified())) {
            if (u.isEmailVerified()) {
                qp.setQuota(u, Paths.get(""), StoreManager.SPACE_QUOTA_SIZE);
            } else {
                qp.setQuota(u, Paths.get(""), StoreManager.SPACE_QUOTA_SIZE_NO_VERIFIED_USER);
            }
        }

        return u;
    }

    @Override
    public DataUser createUser(DataUser user) throws UserException {
        DataUser u = null;
        Start.getUserManager().getUserProvider().createUser(user);
        u = getUser(user.getEmail());
        ContextStore.createUserContext(u);
        return u;
    }

    @Override
    public boolean deleteUser(DataUser user) throws UserException, QuotaException {
        boolean deleted = false;
        Start.getUserManager().getUserProvider().deleteUser(user);
        qp.removeQuota(user, Paths.get(""));
        sp.delete(user, Paths.get(""));

        deleted = true;
        return deleted;
    }


    @Override
    public Users getAllUsers() {
        Users u = null;
        try {
            u = Start.getUserManager().getUserProvider().getUsers();
        } catch (UserException e) {
            e.printStackTrace();
        }
        return u;
    }

    @Override
    public Users search(String query) {
        UsersFound found = usp.search(query);
        Users users = new Users();
        users.addAll(found.getAll());
        return users;
    }

    @Override
    public AvailableDescriptor disableUser(DataUser user, String reason) throws DisablingException, UserException {
        avp.disableUser(user, reason);
        return getAvialableDescriptor(user);
    }

    @Override
    public AvailableDescriptor enableUser(DataUser user) throws EnablingException, UserException {
        avp.enableUser(user);
        return getAvialableDescriptor(user);
    }

    @Override
    public AvailableDescriptor getAvialableDescriptor(DataUser user) throws UserException {
        return avp.getDescriptor(user);
    }

    /**
     * @param user
     */
    @Override
    public ContentSummary getContentSummary(DataUser user) {
        return sp.getContentSummary(user, Paths.get("/"));
    }

    @Override
    public Quota setQuota(DataUser user, Quota q) throws QuotaException {
        return qp.setQuota(user, Paths.get(""), q.getQuota());

    }


}
