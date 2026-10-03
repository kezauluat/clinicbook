package rw.auca.clinicbook.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.entity.RoleName;
import rw.auca.clinicbook.entity.User;
import rw.auca.clinicbook.repository.RoleRepository;
import rw.auca.clinicbook.repository.UserRepository;

/** Creates the first ADMIN account so someone can manage the system. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@clinicbook.rw}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@12345}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = User.builder()
                .fullName("System Admin")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .build();
        admin.getRoles().add(roleRepository.findByName(RoleName.ADMIN).orElseThrow());
        userRepository.save(admin);
        log.info("Seeded admin user {}", adminEmail);
    }
}
