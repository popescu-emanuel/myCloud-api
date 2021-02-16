package fmi.unibuc.ro.mycloudapi.encryption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


@Component
@Slf4j
public class RSAUtils {

    private final static String algorithm = "RSA";
    private final static int KEY_SIZE = 1024;

    public boolean generateKey(String publicKeyOutput, String privateKeyOutput) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
            keyGen.initialize(KEY_SIZE);

            final KeyPair key = keyGen.generateKeyPair();

            final File publicKeyFile = new File(publicKeyOutput);
            publicKeyFile.getParentFile().mkdirs();
            publicKeyFile.createNewFile();

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(publicKeyFile))) {
                dos.write(key.getPublic().getEncoded());
            }

            final File privateKeyFile = new File(privateKeyOutput);
            privateKeyFile.getParentFile().mkdirs();
            privateKeyFile.createNewFile();
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(privateKeyFile))) {
                dos.write(key.getPrivate().getEncoded());
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public byte[] encrypt(PublicKey key, byte[] data) {
        try {

            final Cipher cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(data);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }

    public byte[] decrypt(PrivateKey key, byte[] encryptedData) {

        try {

            final Cipher cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.DECRYPT_MODE, key);

            return cipher.doFinal(encryptedData);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

    }

    public PublicKey getPublicKey(String publicKeyPath) throws Exception {
        return KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(Files.readAllBytes(Paths.get(publicKeyPath))));
    }

    public PrivateKey getPrivateKey(String privateKeyPath) throws Exception {
        return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get(privateKeyPath))));
    }

    public PublicKey getPublicKey(byte[] encryptedPublicKey) throws Exception {
        return KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(encryptedPublicKey));
    }

    public PrivateKey getPrivateKey(byte[] encryptedPrivateKey) throws Exception {
        return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(encryptedPrivateKey));
    }

}
