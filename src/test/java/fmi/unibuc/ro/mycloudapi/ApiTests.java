package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.payload.request.DirectorySpecification;
import fmi.unibuc.ro.mycloudapi.payload.request.UploadFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.response.JwtResponse;
import fmi.unibuc.ro.mycloudapi.payload.response.UploadFileResponse;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ApiTests extends AuthorizationTestUtil {

    @LocalServerPort
    private int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;

    private final String MOCK_TEXT_FILE = "documents/mock-text-file.txt";

    @Before
    public void setup() {
        userRepository.deleteAll();
        restTemplate
                .postForObject("http://localhost:" + port + "/api/auth/signup", signUpRequest, String.class);
        JwtResponse jwtResponse = restTemplate
                .postForObject("http://localhost:" + port + "/api/auth/signin", signUpRequest, JwtResponse.class);
        token = jwtResponse.getToken();
        log.warn("Token received: {}", token);
    }

    @Test
    public void givenCredentials_whenUploadFile_thenFoundFile() {
        final String bearerToken = "Bearer " + token;
        log.warn("Upload file using token {}", bearerToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", bearerToken);

        UploadFileSpecification uploadFileSpecification = new UploadFileSpecification();
        uploadFileSpecification.setBreadcrumb(new ArrayList<>());
        uploadFileSpecification.setFile(mockFile());

        HttpEntity<String> entity = new HttpEntity<>(
                uploadFileSpecification.toString(),
                headers);

        restTemplate.exchange(
                "http://localhost:" + port + "/api/cloud/upload",
                HttpMethod.POST,
                entity,
                UploadFileResponse.class);

        // todo: Check if file was stored
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
