package orchi.HHCloud.cipher;

import orchi.HHCloud.Start;

public class CipherManager {

    private static CipherManager instance = new CipherManager();
    private String cipherClassName = Start.conf.getString("cipher.provider");
    private CipherProvider cipherProvider;

    public CipherManager() {
        try {
            Class<?> classCipherProvider = Class.forName(cipherClassName);

            setCipherProvider((CipherProvider) classCipherProvider.newInstance());

            getCipherProvider().init();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static CipherManager getInstance() {
        if (instance == null) {
            instance = new CipherManager();
        }
        return instance;
    }

    /**
     * @return the cipherProvider
     */
    public CipherProvider getCipherProvider() {
        return cipherProvider;
    }

    /**
     * @param cipherProvider the cipherProvider to set
     */
    private void setCipherProvider(CipherProvider cipherProvider) {
        this.cipherProvider = cipherProvider;
    }

}
