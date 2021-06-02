package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.payload.request.LoginRequest;
import fmi.unibuc.ro.mycloudapi.payload.request.SignUpRequest;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AuthorizationTestUtil {

    public final String MOCK_TEXT_FILE = "documents/mock-text-file.txt";

    public static final SignUpRequest signUpRequest;
    public static final LoginRequest loginRequest;
    public static final LoginRequest invalidLoginRequest;

    static {
        signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("registrationTest@gmail.com");
        signUpRequest.setPassword("testPassword");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("testPassword");

        invalidLoginRequest = new LoginRequest();
        loginRequest.setEmail("invalid@gmail.com");
        loginRequest.setPassword("testPassword");
    }

    @SneakyThrows
    public File readResourceFromTest(String fullPath){
        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fullPath);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fullPath);
        } else {
            return new File("a");
        }
    }

    public MultipartFile mockFile(){
        Path path = Paths.get(MOCK_TEXT_FILE);
        File f = new File(path.toUri());
        String name = f.getName();
        String originalFileName = f.getName();
        String contentType = "text/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
        }

        MultipartFile result = new MockMultipartFile(name, originalFileName, contentType, content);
        return result;
    }

}
