package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.exception.authorization.UserNotAuthenticatedException;
import fmi.unibuc.ro.mycloudapi.payload.request.UploadFileSpecification;
import fmi.unibuc.ro.mycloudapi.payload.response.JwtResponse;
import fmi.unibuc.ro.mycloudapi.payload.response.UploadFileResponse;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import fmi.unibuc.ro.mycloudapi.security.services.UserDetailsImpl;
import fmi.unibuc.ro.mycloudapi.util.AuthenticationUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class ExceptionsTests extends AuthorizationTestUtil {

    @LocalServerPort
    private int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    AuthenticationUtil authenticationUtil;

    private String token;

    @Before
    public void init() {
    }

    @Test
    public void givenInvalidCredentials_whenLogin_thenThrowException() {
        final String loginUrl = "http://localhost:" + port + "/api/auth/signin";
        final String loginResponse = restTemplate.postForObject(
                loginUrl,
                invalidLoginRequest,
                String.class
        );
        Assertions.assertTrue(loginResponse.toLowerCase().contains("bad request"));
    }

    @Test
    public void givenVisitorSession_whenRetrieveUserDetails_thenThrowException() {
        try {
            final UserDetailsImpl authenticatedUser = authenticationUtil.getPrincipal();
            Assertions.assertNull(authenticatedUser);
        } catch (UserNotAuthenticatedException e){
            assert true;
        }
    }

    @Test
    public void givenUserSession_whenRequestAdminPage_thenThrowException(){
        final String loginUrl = "http://localhost:" + port + "/api/auth/signin";
        final JwtResponse loginResponse = restTemplate
                .postForObject(loginUrl, loginRequest, JwtResponse.class);
        this.token = loginResponse.getToken();

        final String getUserUrl = "http://localhost:" + port + "/api/admin/users";
        final String getUsersResponse = restTemplate
                .getForObject(getUserUrl, String.class);
        Assertions.assertTrue(getUsersResponse.contains("Unauthorized"));
    }

}
