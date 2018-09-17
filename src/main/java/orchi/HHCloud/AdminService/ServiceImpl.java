package orchi.HHCloud.AdminService;

import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaException;
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
        DataUser oldU = null;

        try {
            oldU = (DataUser) Start.getUserManager().getUserProvider().getUserById(user.getId());
            u = (DataUser) Start.getUserManager().getUserProvider().editUser(user);
            if ((oldU.isEmailVerified() != u.isEmailVerified())) {
                if (u.isEmailVerified()) {
                    qp.setQuota(u, Paths.get(""), StoreManager.SPACE_QUOTA_SIZE);
                } else {
                    qp.setQuota(u, Paths.get(""), StoreManager.SPACE_QUOTA_SIZE_NO_VERIFIED_USER);
                }
            }
        } catch (UserException e) {
            e.printStackTrace();
        } catch (QuotaException e) {
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
            ContextStore.createUserContext(u);
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
            qp.removeQuota(user, Paths.get(""));
            sp.delete(user, Paths.get(""));

            deleted = true;

        } catch (UserException e) {
            e.printStackTrace();
        } catch (QuotaException e) {
            e.printStackTrace();
        }
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

}
