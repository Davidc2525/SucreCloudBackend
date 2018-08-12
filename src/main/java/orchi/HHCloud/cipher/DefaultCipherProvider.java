package orchi.HHCloud.cipher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.net.util.Base64;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.crypto.tink.aead.AeadKeyTemplates;

public class DefaultCipherProvider implements CipherProvider {

	private Aead aead;

	@Override
	public void init() {
		try {
			AeadConfig.register();
			String keysetFilename = Thread.class.getResource("/").getPath() + "keyset.json";
			KeysetHandle keysetHandle = null;
			// 1. Obtener o generar KeySet
			try {
				keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File(keysetFilename)));
			} catch (FileNotFoundException e) {

				keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);

				CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(keysetFilename)));

			}

			String plaintext = "David23030487";
			// 2. Obtener primitiva
			aead = AeadFactory.getPrimitive(keysetHandle);

		} catch (GeneralSecurityException | IOException e) {

			e.printStackTrace();
		}

	}

	@Override
	public String encryptString(String plainString) {

		byte[] ciphertext = null;
		try {
			ciphertext = aead.encrypt(plainString.getBytes(), "pass".getBytes());
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Base64.encodeBase64String(ciphertext);
	}

	@Override
	public String decryptPassword(String encrtpStrint) {
		byte[] decrypted = null;
		try {
			decrypted = aead.decrypt(Base64.decodeBase64(encrtpStrint), "pass".getBytes());
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String(decrypted);
	}

}
