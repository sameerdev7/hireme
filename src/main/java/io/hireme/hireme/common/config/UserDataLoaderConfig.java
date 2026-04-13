package io.hireme.hireme.common.config; // Or wherever you put your configuration/runner

import io.hireme.hireme.user.User;
import io.hireme.hireme.user.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("api")
@Log4j2
public class UserDataLoaderConfig {
    

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,

            // Injecting Admin properties
            @Value("${app.security.admin.username}") String adminUsername,
            @Value("${app.security.admin.password}") String adminPassword,
            @Value("${app.security.admin.role}") String adminRole
        ) {
        
        return args -> {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(adminRole);

            userRepository.findByUsername(adminUsername).ifPresentOrElse(
                existingUser -> {
                    existingUser.setPassword(passwordEncoder.encode(adminPassword));
                    userRepository.save(existingUser);
                    log.info("Updated Admin User password: {}", adminUsername);
                },
                () -> {
                    userRepository.save(admin);
                    log.info("Initialized Admin User: {}", adminUsername);
                }
            );
        };
    }
}
