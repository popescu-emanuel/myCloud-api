package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.payload.response.JwtResponse;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotAuthenticatedTests extends AuthorizationTestUtil {

    @LocalServerPort
    private int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void whenNoAuth_thenProvidePublicContent() {
        final String url = "http://localhost:" + port + "/api/test/all";
        final String response = this.restTemplate.getForObject(url, String.class);
        assertThat(response).contains("Public Content.");
    }

    @Test
    public void givenSignUpRequest_whenRegister_thenFindEmail() {
        final String url = "http://localhost:" + port + "/api/auth/signup";
        final String response = this.restTemplate.postForObject(url, signUpRequest, String.class);

        Assert.isTrue(userRepository.existsByEmail(signUpRequest.getEmail()));
        Assert.isTrue(response.contains("User registered successfully"));
    }

    @Test
    public void givenLoginRequest_whenAuth_thenReceiveToken() {
        final String loginUrl = "http://localhost:" + port + "/api/auth/signin";
        final JwtResponse loginResponse = restTemplate
                .postForObject(loginUrl, loginRequest, JwtResponse.class);

        Assert.isTrue(userRepository.existsByEmail(loginRequest.getEmail()));
        Assert.notNull(loginResponse.getToken());
    }

}
