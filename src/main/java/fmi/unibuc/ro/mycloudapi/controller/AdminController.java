package fmi.unibuc.ro.mycloudapi.controller;

import fmi.unibuc.ro.mycloudapi.model.User;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> listAllUsers(){
        return ResponseEntity.ok().body(userRepository.findAll());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> listAllUsers(@PathVariable Long id){
        log.debug("Request data for account identified by {}", id);
        final Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isPresent()){
            log.debug("Request data found for account identified by {}", id);
            return ResponseEntity.ok(optionalUser.get());
        } else {
            log.debug("Request data not found for account identified by {}", id);
            return ResponseEntity.badRequest().body("There is no account identified by " + id);
        }
    }

}
