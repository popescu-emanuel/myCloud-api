package fmi.unibuc.ro.mycloudapi.service;

import fmi.unibuc.ro.mycloudapi.exception.database.DuplicateEmailException;
import fmi.unibuc.ro.mycloudapi.model.User;
import fmi.unibuc.ro.mycloudapi.payload.response.MemoryAllocationResponse;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import fmi.unibuc.ro.mycloudapi.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUtils fileUtils;

    public boolean checkPassword(String email, String password) {
        log.info("Check password for " + email);
        final Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isPresent()) {
            log.info("User found in the database " + email);
            User user = maybeUser.get();
            String userPassword = user.getPassword();
            return passwordEncoder.matches(password, userPassword);
        }
        return false;
    }

    public void updatePassword(String email, String password){
        log.info("Update password for " + email);
        final Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }
        throw new UsernameNotFoundException(email);
    }

    public void updateEmail(String oldEmail, String newEmail){
        log.info("Update email for " + oldEmail);
        final Optional<User> maybeUser = userRepository.findByEmail(oldEmail);
        if (maybeUser.isPresent()) {
            final Boolean existsByEmail = userRepository.existsByEmail(newEmail);
            if(!existsByEmail){
                User user = maybeUser.get();
                user.setEmail(newEmail);
                userRepository.save(user);
            } else {
                throw new DuplicateEmailException(newEmail);
            }
        }
        throw new UsernameNotFoundException(oldEmail);
    }

    public MemoryAllocationResponse getDataUsage(String email) throws IOException {
        final Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()){
            log.warn("User could not be found in the database");
            throw new UsernameNotFoundException("User " + email + " not found. Aborting");
        }

        final Path userStorageLocation = fileUtils.getStorageLocation();
        final long usedMemory = Files.walk(userStorageLocation)
                .map(Path::toFile)
                .filter(File::isFile)
                .mapToLong(File::length)
                .sum();

        /**
            Total capacity = 10GB = 10 * Math.pow(1024,4)   // could be easily computed
            In use         = 9MB                            // already in bytes
            Free space      = Total capacity - In use = (Total Capacity in Bytes - In use in Bytes).byteCountToDisplaySize
         */


        final Integer totalMemory = user.get().getSizePlan().getCapacity();
        final long totalMemoryBytes = user.get().getSizePlan().getCapacity() * Math.round(Math.pow(1024, 3));

        final long freeMemory = totalMemoryBytes - usedMemory;
        String freeMemoryDisplay = org.apache.commons.io.FileUtils.byteCountToDisplaySize(freeMemory);

        final String usedMemoryDisplay = org.apache.commons.io.FileUtils.byteCountToDisplaySize(usedMemory);

        MemoryAllocationResponse memoryAllocationResponse = new MemoryAllocationResponse(
                totalMemory + " GB",
                freeMemoryDisplay,
                usedMemoryDisplay
        );

        return memoryAllocationResponse;
    }

}
