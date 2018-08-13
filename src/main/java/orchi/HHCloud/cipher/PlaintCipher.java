package orchi.HHCloud.cipher;

public class PlaintCipher implements CipherProvider {

	@Override
	public void init() {
		
	}

	@Override
	public String encrypt(String plainString) {
		return plainString;
	}

	@Override
	public String decrypt(String encrtpStrint) {
		return encrtpStrint;
	}

}
