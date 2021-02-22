package fmi.unibuc.ro.mycloudapi;

import fmi.unibuc.ro.mycloudapi.properties.FileStorageProperties;
import fmi.unibuc.ro.mycloudapi.model.*;
import fmi.unibuc.ro.mycloudapi.repositories.RoleRepository;
import fmi.unibuc.ro.mycloudapi.repositories.SizePlanRepository;
import fmi.unibuc.ro.mycloudapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FileStorageProperties fileStorageProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SizePlanRepository sizePlanRepository;

    @Override
    public void run(String... args) throws Exception {

        boolean override = false;
        initFolders(override);
        initPlans();
        initAccounts();
    }

    private void initPlans() {
        try{
            final boolean basicPlanExist = sizePlanRepository.findSizePlanByType(SubscriptionType.BASIC) != null;
            if(!basicPlanExist){
                SizePlan basicSizePlan = new SizePlan(SubscriptionType.BASIC, 5);
                sizePlanRepository.save(basicSizePlan);
            }

            final boolean paidPlanExist = sizePlanRepository.findSizePlanByType(SubscriptionType.PAID) != null;
            if(!paidPlanExist){
                SizePlan paidSizePlan = new SizePlan(SubscriptionType.PAID, 10);
                sizePlanRepository.save(paidSizePlan);
            }
        } catch (Exception e){
            log.warn("Could not initialize size plans, as they already exist.");
            e.printStackTrace();
        }

    }

    private void initFolders(boolean override) {
        String uploadDir = fileStorageProperties.getUploadDir();
        String keysDir = fileStorageProperties.getKeysDir();

        initializeFolder(uploadDir, override);
        initializeFolder(keysDir, override);
    }

    private void initializeFolder(String directoryPath, boolean override) {
        try {
            final Path path = Paths
                    .get(directoryPath)
                    .normalize()
                    .toAbsolutePath();
            if (override) {
                FileUtils.deleteDirectory(new File(path.toUri()));
                log.info("Directory removed {}", directoryPath);
            }
            if(!Files.exists(path)){
                Path directory = Files.createDirectory(path);
                log.info("Directory initialized at {}", directoryPath);
            }
        } catch (IOException e) {
            if (!override) {
                log.warn("Directory was previously created {}", directoryPath);
            } else {
                log.warn("Could not create directory {}", directoryPath);
            }
        }
    }

    private void initAccounts() {
        final Role adminRole = initializeRole(ERole.ROLE_ADMIN);
        final Role userRole = initializeRole(ERole.ROLE_USER);
        final Role moderatorRole = initializeRole(ERole.ROLE_MODERATOR);
        log.info("Roles initialized");

        Set<Role> allRights = Set.of(
                adminRole,
                userRole,
                moderatorRole
        );

        initializeAccount("admin@gmail.com", "qq11qq11", SubscriptionType.PAID, allRights);
        initializeAccount("moderator@gmail.com", "qq11qq11", SubscriptionType.BASIC, Set.of(moderatorRole));
        initializeAccount("user@gmail.com", "qq11qq11", SubscriptionType.BASIC, Set.of(userRole));
        initializeAccount("test@gmail.com", "testPassword", SubscriptionType.BASIC, Set.of(userRole));
        log.info("Accounts initialized");

    }

    private Role initializeRole(ERole role) {
        if (roleRepository.findByName(role).isEmpty()) {
            Role newRole = new Role(role);
            final Role save = roleRepository.save(newRole);
            log.info("Role {} created", newRole);
            return save;
        }
        return roleRepository.findByName(role).get();
    }

    private User initializeAccount(String email, String password, SubscriptionType type, Set<Role> roles) {
        if (userRepository.findByEmail(email).isEmpty()) {
            final SizePlan plan = sizePlanRepository.findSizePlanByType(type);

            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setSizePlan(plan);
            user.setRoles(roles);

            final User save = userRepository.save(user);
            log.info("User {} registered with roles {}", email, roles);
            return save;
        }
        return userRepository.findByEmail(email).get();
    }
}
