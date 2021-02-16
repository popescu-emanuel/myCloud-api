package fmi.unibuc.ro.mycloudapi.encryption;

import fmi.unibuc.ro.mycloudapi.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * A simple utility class for easily encrypting and decrypting data using the AES algorithm.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AESUtils {

	private static final String ALGORITHM = "AES";
	private static final String ALGORITHM_IMPLEMENTATION = "AES/ECB/PKCS5Padding";
	private static final Integer AES_KEY_SIZE = 128;
	private final FileUtils fileUtils;

	private static final String HASHING_ALGORITHM = "SHA-1";

	public byte[] encrypt(SecretKey secretKey, byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_IMPLEMENTATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		} catch (Exception ex) {
			log.error("Exception when encrypting data using AES {}", ex.getStackTrace());
			ex.printStackTrace();
		}
		return null;
	}

	public byte[] decrypt(SecretKey secretKey, byte[] encrypted) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_IMPLEMENTATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(encrypted);
		} catch (Exception ex) {
			log.error("{}", ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}

	public byte[] decrypt(String password, byte[] encrypted) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_IMPLEMENTATION);
			cipher.init(Cipher.DECRYPT_MODE, generateSecretKey(password));
			return cipher.doFinal(encrypted);
		} catch (Exception ex) {
			log.error("Exception when decrypting data using AES {}", ex.getStackTrace());
			ex.printStackTrace();
		}
		return null;
	}

	public void writeSecretKey(SecretKey key, File file) throws IOException {
		file.getParentFile().mkdirs();
		file.createNewFile();
		try (FileOutputStream fis = new FileOutputStream(file)) {
			fis.write(key.getEncoded());
		} catch (Exception ex){
			log.error("{}", ex.getMessage());
			ex.printStackTrace();
		}
	}

	public SecretKey extractSecretKey(File file) throws IOException {
		return new SecretKeySpec(Files.readAllBytes(file.toPath()), ALGORITHM);
	}

	public SecretKey generateSecretKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
			keyGenerator.init(AES_KEY_SIZE);
			return keyGenerator.generateKey();
		} catch (Exception ex) {
			log.error("{}", ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}

	public SecretKey generateSecretKey(String password) {
		try {
			byte[] key = password.getBytes(StandardCharsets.UTF_8);
			MessageDigest sha = MessageDigest.getInstance(HASHING_ALGORITHM);
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // use only first 128 bit

			return new SecretKeySpec(key, ALGORITHM);
		} catch (Exception ex) {
			log.error("{}", ex.getMessage());
			ex.printStackTrace();
		}

		return null;
	}

	private SecretKey generateSecretKey(byte[] salt, String password) {
		try {
			byte[] key = (salt + password).getBytes(StandardCharsets.UTF_8);
			MessageDigest sha = MessageDigest.getInstance(HASHING_ALGORITHM);
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // use only first 128 bit

			return new SecretKeySpec(key, ALGORITHM);
		} catch (Exception ex) {
			log.error("{}", ex.getMessage());
			ex.printStackTrace();
		}

		return null;
	}

}
