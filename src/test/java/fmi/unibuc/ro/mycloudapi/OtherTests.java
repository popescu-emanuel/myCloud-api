package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.encryption.AESUtils;
import fmi.unibuc.ro.mycloudapi.encryption.EncryptionUtils;
import fmi.unibuc.ro.mycloudapi.payload.request.DirectorySpecification;
import fmi.unibuc.ro.mycloudapi.util.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;

@SpringBootTest
@Slf4j
public class OtherTests {

    private static final String MOCK_TEXT_FILE = "documents/mock-text-file.txt";

    @Autowired
    AESUtils aesUtils;

    @Autowired
    EncryptionUtils encryptionUtils;

    @Autowired
    FileUtils fileUtils;

//    @Autowired
//    CredentialsProperties credentialsProperties;

    private File testFile;
    private String email;

    @BeforeEach
    @SneakyThrows
    void initialize() {
        testFile = ResourceUtils.getFile("classpath:" + MOCK_TEXT_FILE);
        email = "test@gmail.com";
    }

    @Test
    @SneakyThrows
    void givenEmailAndFile_whenStoreFile_thenFindInCorrectPath() {
//        final byte[] byteContent = org.apache.commons.io.FileUtils.readFileToByteArray(testFile);
//        fileUtils.storeFileFor(email, testFile.getName(), byteContent);
//
//        final Path storageLocationFor = fileUtils.getStorageLocationFor(email, testFile.getName());
//        File check = new File(storageLocationFor.toUri());
//        assert check.exists();
    }

    @Test
    @SneakyThrows
    void givenPassword_whenHashedMultipleTimes_thenReturnTheSameResult() {
        String password = "a1232@sdsa344@@#!S$s";
        String password2 = "a1232@sdsa344@@#!S$s";

        final SecretKey spec1 = aesUtils.generateSecretKey(new String(hash(password)));
        final SecretKey spec2 = aesUtils.generateSecretKey(new String(hash(password2)));
        log.info("{}", spec1.getEncoded());
        log.info("{}", spec2.getEncoded());


        assert Arrays.equals(spec1.getEncoded(), spec2.getEncoded());

    }

    public byte[] hash(String text) {
        byte[] hashedText = text.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA3-256");
//            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            hashedText = sha.digest(hashedText);
            log.debug("Key is {}", new String(hashedText));
        } catch (Exception e) {
            log.error("Error when hashing text {}", text);
            e.printStackTrace();
        }
        return hashedText;
    }

}
