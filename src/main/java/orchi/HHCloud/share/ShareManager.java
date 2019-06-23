package orchi.HHCloud.share;

import orchi.HHCloud.provider.GetProvider;
import orchi.HHCloud.provider.ProviderManager;
import orchi.HHCloud.provider.ProviderManagerInstance;
import orchi.HHCloud.Start;

/**
 * @author david
 */
@ProviderManager
public class ShareManager {
    private static String defaultShareProvider = Start.conf.getString("share.sharemanager.provider");
    private static ShareProvider shareProvider;
    ;
    private static ShareManager instance;

    public ShareManager() {
        try {
            Class<?> classProvider = Class.forName(defaultShareProvider);

            shareProvider = (ShareProvider) classProvider.newInstance();
            shareProvider.init();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.exit(1);
            e.printStackTrace();
        }
    }

    @ProviderManagerInstance
    public static ShareManager getInstance() {
        if (instance == null) {
            instance = new ShareManager();
        }
        return instance;
    }

    @GetProvider
    public ShareProvider getShareProvider() {
        return shareProvider;
    }
}
