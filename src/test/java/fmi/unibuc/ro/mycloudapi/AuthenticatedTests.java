package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.payload.response.JwtResponse;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class AuthenticatedTests extends AuthorizationTestUtil {

    @LocalServerPort
    private int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;

    @Before
    public void init() {
        final String loginUrl = "http://localhost:" + port + "/api/auth/signin";
        final JwtResponse loginResponse = restTemplate
                .postForObject(loginUrl, loginRequest, JwtResponse.class);
        this.token = loginResponse.getToken();
        log.info("Token for testing purpose: {}", token);
    }

    @Test
    @SneakyThrows
    public void givenCredentials_whenUploadFile_thenFoundFile() {
//        final String uploadFileUrl = "http://localhost:" + port + "/api/cloud/upload";
//
//        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
//        final File file = new File(Paths.get(MOCK_TEXT_FILE).toUri());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//
////        parameters.add("file", new MultipartFileResource(mockFile().getInputStream()));
//        parameters.add("breadcrumb", "");
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.add("Authorization", "Bearer " + token);
//        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                uploadFileUrl,
//                HttpMethod.POST,
//                entity,
//                String.class
//        );
//
//        int a = 5;

    }

    @Test
    @SneakyThrows
    public void givenCredentials_whenRecomputePassword_thenFileCouldBeDecrypted() {

    }

    @Test
    public void givenExpiredToken_whenAccessFiles_thenThrowError() {

    }

}
