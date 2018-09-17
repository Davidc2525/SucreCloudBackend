package orchi.HHCloud.AdminService;

import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.quota.Quota;
import orchi.HHCloud.quota.QuotaProvider;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.store.ContextStore;
import orchi.HHCloud.store.StoreManager;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Users;

import java.nio.file.Paths;

public class ServiceImpl implements Service {
    private QuotaProvider qp = Start.getQuotaManager().getProvider();
    private StoreProvider sp = Start.getStoreManager().getStoreProvider();

    @Override
    public int suma(int a, int b) {
        return a + b;
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

    /**
     *
     * @param user
     */
    @Override
    public ContentSummary getContentSummary(DataUser user) {
        return sp.getContentSummary(user,Paths.get("/"));
    }

    @Override
    public Quota setQuota(DataUser user,Quota q) throws QuotaException {
        return qp.setQuota(user,Paths.get(""),q.getQuota());

    }


}
