package orchi.HHCloud.quota;

import orchi.HHCloud.Start;

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

    public static QuotaManager getInstance() {
        if (instance == null) {
            instance = new QuotaManager();
        }
        return instance;
    }

    public QuotaProvider getProvider() {
        return provider;
    }
}
