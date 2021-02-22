package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.encryption.AESUtils;
import fmi.unibuc.ro.mycloudapi.util.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.util.Arrays;

@SpringBootTest
@Slf4j
public class Base64EncodingTests {

    @Autowired
    private FileUtils filesUtil;

    @Autowired
    private AESUtils aesUtils;

    private static final String MOCK_PDF_FILE = "documents/mock-pdf-file.pdf";

    @Test
    @SneakyThrows
    void givenPDFFile_whenEncodeDecode_thenReturnOriginal(){
        byte[] fileContent = filesUtil.readFileFromClasspathAsByteArray(MOCK_PDF_FILE);

        SecretKey secretKey = aesUtils.generateSecretKey("emiemi");
        byte[] encryptedData = aesUtils.encrypt(secretKey, fileContent);

        Base64 base64 = new Base64();
        byte[] base64EncryptedTextByte = base64.encode(encryptedData);
        byte[] base64DecryptedTextByte = base64.decode(base64EncryptedTextByte);

        byte[] decryptedData = aesUtils.decrypt("emiemi", base64DecryptedTextByte);

        assert Arrays.equals(fileContent, decryptedData);

    }

}
