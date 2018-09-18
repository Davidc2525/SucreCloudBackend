package orchi.HHCloud.cipher;

public interface CipherProvider {
    public void init();

    public String encrypt(String plainString);

    public String decrypt(String encrtpStrint);
}
