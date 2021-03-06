package fmi.unibuc.ro.mycloudapi.controller;

import fmi.unibuc.ro.mycloudapi.model.ERole;
import fmi.unibuc.ro.mycloudapi.model.Role;
import fmi.unibuc.ro.mycloudapi.model.User;
import fmi.unibuc.ro.mycloudapi.payload.request.LoginRequest;
import fmi.unibuc.ro.mycloudapi.payload.request.SignUpRequest;
import fmi.unibuc.ro.mycloudapi.payload.response.JwtResponse;
import fmi.unibuc.ro.mycloudapi.payload.response.MessageResponse;
import fmi.unibuc.ro.mycloudapi.repositories.RoleRepository;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import fmi.unibuc.ro.mycloudapi.security.jwt.JwtUtils;
import fmi.unibuc.ro.mycloudapi.security.services.UserDetailsImpl;
import fmi.unibuc.ro.mycloudapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;

    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        if (!userRepository.existsByEmail(loginRequest.getEmail())) {
            String message = String.format("Login data is not valid");
            return ResponseEntity
                    .badRequest()
                    .body(new UsernameNotFoundException(message));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                roles,
                userRepository.findById(userDetails.getId()).get().getSizePlan()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        userService.fromRegisterRequestCreateUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


}
