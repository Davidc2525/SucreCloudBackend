package orchi.HHCloud.store;

import orchi.HHCloud.user.User;

import java.nio.file.Path;

public interface QuotaProvider {
    public void setQuota(User user, Path path, long size);

    public void removeQuota(User user,Path path);
}
