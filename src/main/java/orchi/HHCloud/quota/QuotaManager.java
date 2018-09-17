package orchi.HHCloud.quota;
import orchi.HHCloud.Start;

public class QuotaManager {
    private static QuotaManager instance;
    private static String nameProvider = Start.conf.getString("quota.quotamanager.quotaprovider");
    private QuotaProvider provider;


    public QuotaManager(){
        try {
            Class<? extends QuotaProvider> classProvider = (Class<? extends QuotaProvider>) Class.forName(nameProvider);

            provider = classProvider.newInstance();
            provider.init();

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public QuotaProvider getProvider(){
        return provider;
    }

    public static QuotaManager getInstance(){
        if(instance == null){
            instance = new QuotaManager();
        }
        return instance;
    }
}
