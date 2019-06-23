package orchi.HHCloud.mail;

import orchi.HHCloud.provider.GetProvider;
import orchi.HHCloud.provider.ProviderManager;
import orchi.HHCloud.provider.ProviderManagerInstance;
import orchi.HHCloud.Start;

@ProviderManager
public class MailManager {
    private static MailManager instance;
    private static String nameProvider = Start.conf.getString("mail.mailmanager.mail.provider");
    private MailProvider provider;


    public MailManager() {
        try {
            Class<? extends MailProvider> classProvider = (Class<? extends MailProvider>) Class.forName(nameProvider);

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
    public static MailManager getInstance() {
        if (instance == null) {
            instance = new MailManager();
        }
        return instance;
    }

    @GetProvider
    public MailProvider getProvider() {
        return provider;
    }
}
