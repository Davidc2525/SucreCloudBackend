package orchi.HHCloud.quota;

import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.User;

import java.nio.file.Path;

public abstract class ProxyQuotaProvider implements QuotaProvider {
    private StoreProvider storeProvider;

    @Override
    public void init() {
        storeProvider = Start.getStoreManager().getStoreProvider();
    }

    @Override
    public Quota setQuota(User user, Path path, long size) throws QuotaException {
        storeProvider.setQuota(user, path, size);
        return null;
    }

    @Override
    public Quota getQuota(User user) throws QuotaException {
        storeProvider.getQuota(user);
        return null;
    }

    @Override
    public void removeQuota(User user, Path path) throws QuotaException {
        storeProvider.removeQuota(user, path);
    }
}
