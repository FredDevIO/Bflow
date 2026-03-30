package io.github.lucyfred.bflow.config;

import io.github.lucyfred.bflow.entity.User;
import io.github.lucyfred.bflow.enums.Role;
import io.github.lucyfred.bflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements  CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:admin@bflow.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:bflow}")
    private String adminPassword;

    @Value("${ADMIN_NAME:admin}")
    private String adminName;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.valueOf("ADMIN"));

            userRepository.save(admin);
            System.out.println("[INIT] User created successfully: " + adminEmail);
        } else {
            System.out.println("[INIT] User already exists.");
        }
    }
}
