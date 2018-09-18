package orchi.HHCloud.mail;

import orchi.HHCloud.Start;

public class MailManager {
    private static MailManager instance;
    private static String nameProvider = Start.conf.getString("mail.mailmanager.defaultmailprovider");
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

    public static MailManager getInstance() {
        if (instance == null) {
            instance = new MailManager();
        }
        return instance;
    }

    public MailProvider getProvider() {
        return provider;
    }
}
