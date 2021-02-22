package fmi.unibuc.ro.mycloudapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import fmi.unibuc.ro.mycloudapi.payload.request.UploadFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.response.JwtResponse;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import io.jsonwebtoken.lang.Assert;
import org.assertj.core.util.Lists;
import org.hibernate.internal.util.StringHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    }

    @Test
    public void givenCredentials_whenUploadFile_thenFoundFile() {
        final String uploadFileUrl = "http://localhost:" + port + "/api/cloud/upload";

        UploadFileSpecification uploadFileSpecification = new UploadFileSpecification();
        uploadFileSpecification.setFile(mockFile());
        uploadFileSpecification.setBreadcrumb(Lists.emptyList());

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            final String uploadResponse = restTemplate
                    .postForObject(uploadFileUrl, objectMapper.writeValueAsString(uploadFileSpecification), String.class);
            Assert.notNull(uploadResponse);
        } catch (Exception e){
            assert false;
        }

    }

    @Test
    public void givenCredentials_whenRecomputePassword_thenFileCouldBeDecrypted() {

    }

    @Test
    public void givenExpiredToken_whenAccessFiles_thenThrowError() {

    }

}
