package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.encryption.AESUtils;
import fmi.unibuc.ro.mycloudapi.encryption.RSAUtils;
import fmi.unibuc.ro.mycloudapi.util.ByteArrayUtil;
import fmi.unibuc.ro.mycloudapi.util.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

@SpringBootTest
@Slf4j
class MyCloudApplicationTests {

	private final String MOCK_TEXT_FILE = "documents/mock-text-file.txt";
	private final String MOCK_PDF_FILE =  "documents/mock-pdf-file.pdf";
	private final String AES_SECRET_KEY = "masterPassword";

	private final String PUBLIC_KEY_PATH = "documents/keys/public.key";
	private final String PRIVATE_KEY_PATH = "documents/keys/private.key";
	private final String AES_KEY_PATH = "documents/keys/aesKeyStore.key";


	@Autowired
	private AESUtils aesUtils;
	
	@Autowired
	private RSAUtils rsaUtils;

	@Autowired
	private FileUtils fileUtils;

	@Autowired
	private ByteArrayUtil byteArrayUtil;

	@Test
	void contextLoads() {
	}

	@SneakyThrows
	private String readFileContent(String filename)
	{
		File file = ResourceUtils.getFile("classpath:" + filename);
		return new String(Files.readAllBytes(file.toPath()));
	}

	@Test
	@SneakyThrows
	/**
	 *  P  = userPassword
	 *  KP = hashedUserPassword
	 *	KF = random128bitKey
	 */
	public void givenStackOverFlowSolution_whenAppliedToData_thenReturnOriginal(){
		String userPassword = "1a2b3c4d5eaaa$#..#.";
		String fileContent = readFileContent(MOCK_PDF_FILE);

		// Hashing the password
		byte[] hashedUserPassword = userPassword.getBytes(StandardCharsets.UTF_8);
		MessageDigest sha = MessageDigest.getInstance("SHA3-256");
		hashedUserPassword = sha.digest(hashedUserPassword);
		log.debug("Key is {}", new String(hashedUserPassword));

		// Generate random 128-bit key
		SecureRandom random = new SecureRandom();
		byte[] randomByteKey = new byte[16]; // 128 bits are converted to 16 bytes;
		random.nextBytes(randomByteKey);

		// Encrypt with AES and randomKey
		final SecretKey secretKey = aesUtils.generateSecretKey(new String(randomByteKey));
		byte[] encryptedFileContent = aesUtils.encrypt(secretKey, fileContent.getBytes());

		// Send file to server

		// Encrypt randomKey KF using AES with HashedPassword KP = CK
		final SecretKey secretKeyForRandomKey = aesUtils.generateSecretKey(new String(hashedUserPassword));

		// The client encrypts KF with KP using AES in ECB mode
		byte[] ck = aesUtils.encrypt(secretKeyForRandomKey, randomByteKey); // CK

		// It also encrypts K′F=KF+1
		byte[] ckp = aesUtils.encrypt(secretKeyForRandomKey, byteArrayUtil.additionToByteArray(randomByteKey, 1)); // CKP

		// Send ck and ckp to server and store under an index

		// Decrypt CK and CK'
		byte[] decryptedCk  = aesUtils.decrypt(new String(hashedUserPassword), ck); // randomKeyDecrypted
		byte[] decryptedCkp = aesUtils.decrypt(new String(hashedUserPassword), ckp);//

		// Check integrity
		assert Arrays.equals(byteArrayUtil.additionToByteArray(decryptedCk, 1), decryptedCkp);

		// Decrypt file on the server
		final byte[] decryptedContent = aesUtils.decrypt(new String(decryptedCk), encryptedFileContent);

		// Check encrypted is the same as decrypted
		assert Arrays.equals(decryptedContent, fileContent.getBytes());
	}

	@Test
	void givenInputText_whenEncryptDecryptWithRSA_thenReturnOriginalContent() throws Exception {
		givenInputData_whenEncryptAndDecryptWithRSA_thenReturnOriginalContent("hello cryptography".getBytes());
	}

	@Test
	void givenInputTextFile_whenEncryptDecryptWithRSA_thenReturnOriginalContent() throws Exception {
		byte[] byteContent = fileUtils.readFileFromClasspathAsByteArray(MOCK_TEXT_FILE);
		givenInputData_whenEncryptAndDecryptWithRSA_thenReturnOriginalContent(byteContent);
	}

	@SneakyThrows
	private void givenInputData_whenEncryptAndDecryptWithRSA_thenReturnOriginalContent(byte[] byteContent){
		rsaUtils.generateKey(PUBLIC_KEY_PATH, PRIVATE_KEY_PATH);

		PublicKey publicKey = rsaUtils.getPublicKey(PUBLIC_KEY_PATH);
		byte[] encrypted = rsaUtils.encrypt(publicKey, byteContent);

		PrivateKey privateKey = rsaUtils.getPrivateKey(PRIVATE_KEY_PATH);
		byte[] decrypted = rsaUtils.decrypt(privateKey, encrypted);

//		log.debug("Original byteContent: " + byteContent);
//		log.debug("Decrypted byteContent: " + new String(decrypted));

		assert Arrays.equals(byteContent, decrypted);
	}

	@Test
	void givenInputText_whenEncryptDecryptWithAES_thenReturnOriginalContent(){
		String content = "Hello cryptography";
		givenInputData_whenEncryptDecryptWithAES_thenReturnOriginal(content.getBytes());
	}

	@Test
	void givenInputTextFile_whenEncryptDecryptWithAES_thenReturnOriginalContent(){
		byte[] fileContent = fileUtils.readFileFromClasspathAsByteArray(MOCK_TEXT_FILE);
		givenInputData_whenEncryptDecryptWithAES_thenReturnOriginal(fileContent);
	}

	@Test
	void givenInputPDFFile_whenEncryptDecryptWithAES_thenReturnOriginalContent() throws Exception {
		byte[] fileContent = fileUtils.readFileFromClasspathAsByteArray(MOCK_PDF_FILE);
		givenInputData_whenEncryptDecryptWithAES_thenReturnOriginal(fileContent);
	}

	private void givenInputData_whenEncryptDecryptWithAES_thenReturnOriginal(byte[] byteContent){

		final SecretKey secretKey = aesUtils.generateSecretKey(AES_SECRET_KEY);
		byte[] encrypted = aesUtils.encrypt(secretKey, byteContent);

		final File aesKeystore = new File(AES_KEY_PATH);
		try {
			aesUtils.writeSecretKey(secretKey, aesKeystore);
		} catch (IOException e) {
			log.error("Could not store AES secret key");
			e.printStackTrace();
		}

		SecretKey decryptionKey = null;
		try {
			decryptionKey = aesUtils.extractSecretKey(aesKeystore);
		} catch (IOException e) {
			log.error("Could not retrieve AES secret key");
			e.printStackTrace();
		}

		final byte[] decrypted = aesUtils.decrypt(decryptionKey, encrypted);

//		log.warn("Original content: " + content);
//		log.warn("Decrypted content: " + new String(decrypted));

		assert Arrays.equals(decrypted, byteContent);
	}

	@Test
	@SneakyThrows
	void givenInputTextFile_whenEncryptDecryptWithAESRSA_thenReturnOriginalContent(){

		byte[] byteContent = fileUtils.readFileFromClasspathAsByteArray(MOCK_TEXT_FILE);

		log.debug("Original byteContent: {}", new String(byteContent));

		final SecretKey secretKey = aesUtils.generateSecretKey(AES_SECRET_KEY);
		byte[] aesEncrypted = aesUtils.encrypt(secretKey, byteContent);
		log.debug("AES byteContent: {}", new String(aesEncrypted));

		rsaUtils.generateKey(PUBLIC_KEY_PATH, PRIVATE_KEY_PATH);

		PublicKey publicKey = rsaUtils.getPublicKey(PUBLIC_KEY_PATH);
		byte[] aesRsaEncrypted = rsaUtils.encrypt(publicKey, aesEncrypted);
		log.debug("AES RSA byteContent: {}", new String(aesRsaEncrypted));

		// todo: Store this file

		PrivateKey privateKey = rsaUtils.getPrivateKey(PRIVATE_KEY_PATH);
		byte[] rsaDecrypted = rsaUtils.decrypt(privateKey, aesRsaEncrypted);
		log.debug("RSA Decrypted byteContent: {}", new String(rsaDecrypted));

		final byte[] aesDecrypted = aesUtils.decrypt(secretKey, rsaDecrypted);
		log.debug("AES Decrypted byteContent: {}", new String(aesDecrypted));

		assert Arrays.equals(aesDecrypted, byteContent);

	}

	@AfterEach
	void cleanDataAfterEach(){
		boolean isRequired = true;
		if(isRequired){
			deleteFileIfExist(PUBLIC_KEY_PATH);
			deleteFileIfExist(PRIVATE_KEY_PATH);
			deleteFileIfExist(AES_KEY_PATH);
		}
	}

	private void deleteFileIfExist(String filename){
		File toRemove = new File(filename);
		if(toRemove.exists()){
			toRemove.delete();
		}
	}

}
