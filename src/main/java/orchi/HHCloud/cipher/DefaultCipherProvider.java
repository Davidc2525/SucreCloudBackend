package orchi.HHCloud.cipher;

import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import org.apache.commons.net.util.Base64;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class DefaultCipherProvider implements CipherProvider {

    private Aead aead;
    private byte[] associatedData = "DATA".getBytes();

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

            // 2. Obtener primitiva
            aead = AeadFactory.getPrimitive(keysetHandle);

        } catch (GeneralSecurityException | IOException e) {

            e.printStackTrace();
        }

    }

    @Override
    public String encrypt(String plainString) {

        byte[] ciphertext = {};
        try {
            ciphertext = aead.encrypt(plainString.getBytes(), associatedData);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return Base64.encodeBase64String(ciphertext);
    }

    @Override
    public String decrypt(String encrtpStrint) {
        byte[] decrypted = {};
        try {
            decrypted = aead.decrypt(Base64.decodeBase64(encrtpStrint), associatedData);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return new String(decrypted);
    }

}
