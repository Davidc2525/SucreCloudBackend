package orchi.HHCloud.quota;

import orchi.HHCloud.provider.GetProvider;
import orchi.HHCloud.provider.ProviderManager;
import orchi.HHCloud.provider.ProviderManagerInstance;
import orchi.HHCloud.Start;

@ProviderManager
public class QuotaManager {
    private static QuotaManager instance;
    private static String nameProvider = Start.conf.getString("quota.quotamanager.quotaprovider");
    private QuotaProvider provider;


    public QuotaManager() {
        try {
            Class<? extends QuotaProvider> classProvider = (Class<? extends QuotaProvider>) Class.forName(nameProvider);

            provider = classProvider.newInstance();
            provider.init();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @ProviderManagerInstance
    public static QuotaManager getInstance() {
        if (instance == null) {
            instance = new QuotaManager();
        }
        return instance;
    }

    @GetProvider
    public QuotaProvider getProvider() {
        return provider;
    }
}
