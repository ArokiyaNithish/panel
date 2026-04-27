package com.campus.recruitment.portal.security;

import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.repository.UserRepository;
import com.campus.recruitment.portal.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                auditService.log(user, "LOGIN", "User logged in successfully");
            });
        }
    }
}
