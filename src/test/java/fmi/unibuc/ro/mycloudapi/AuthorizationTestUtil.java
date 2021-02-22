package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.payload.request.LoginRequest;
import fmi.unibuc.ro.mycloudapi.payload.request.SignUpRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AuthorizationTestUtil {

    private final String MOCK_TEXT_FILE = "documents/mock-text-file.txt";

    SignUpRequest signUpRequest;
    LoginRequest loginRequest;

    {
        signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("registrationTest@gmail.com");
        signUpRequest.setPassword("testPassword");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("testPassword");
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
