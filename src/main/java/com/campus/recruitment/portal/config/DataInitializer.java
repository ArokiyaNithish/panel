package com.campus.recruitment.portal.config;

import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with a default admin account on first startup.
 * Credentials: admin@campus.com / Admin@1234
 * Change the password immediately after first login!
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@campus.com")) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail("admin@campus.com");
            admin.setPassword(passwordEncoder.encode("Admin@1234"));
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            admin.setApproved(true);
            userRepository.save(admin);
            System.out.println("✅ Default admin created: admin@campus.com / Admin@1234");
        }
    }
}
