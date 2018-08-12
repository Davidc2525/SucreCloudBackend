package orchi.HHCloud.cipher;

public interface CipherProvider {
	public void init();
	
	public String encryptString(String plainString);
	
	public String decryptPassword(String encrtpStrint);
}
