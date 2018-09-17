package orchi.HHCloud.quota;

import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.user.User;

import java.nio.file.Path;

public interface QuotaProvider {

    public void init();

    public Quota setQuota(User user, Path path, long size) throws QuotaException;

    public boolean quotaIsSet(User user) throws  QuotaException;

    public Quota getQuota(User user) throws QuotaException;

    public void removeQuota(User user,Path path) throws QuotaException;
}
