package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.payload.response.JwtResponse;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import io.jsonwebtoken.lang.Assert;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotAuthenticatedTests extends AuthorizationTestUtil {

    @LocalServerPort
    private int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void init(){
        userRepository.deleteAll();
    }

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
        // Register
        final String url = "http://localhost:" + port + "/api/auth/signup";
        final String response = this.restTemplate.postForObject(url, loginRequest, String.class);

        // Login
        final String loginUrl = "http://localhost:" + port + "/api/auth/signin";
        final JwtResponse loginResponse = restTemplate
                .postForObject(loginUrl, loginRequest, JwtResponse.class);

        Assert.isTrue(userRepository.existsByEmail(loginRequest.getEmail()));
        Assert.notNull(loginResponse.getToken());
    }

}
