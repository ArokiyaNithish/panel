package com.campus.recruitment.portal.service;

import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.model.StudentProfile;
import com.campus.recruitment.portal.repository.UserRepository;
import com.campus.recruitment.portal.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private StudentProfileRepository studentProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private NotificationService notificationService;

    @Transactional
    public User registerStudent(String fullName, String email, String rawPassword, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(User.Role.STUDENT);
        user.setEnabled(true);
        user.setApproved(true);
        user.setPhone(phone);
        user = userRepository.save(user);

        // Create blank student profile
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        studentProfileRepository.save(profile);

        notificationService.sendNotification(user, "Welcome to Campus Recruitment Portal!",
                "Your account is ready. Complete your profile and upload your resume.", "/student/profile");
        return user;
    }

    @Transactional
    public User registerEmployer(String fullName, String email, String rawPassword, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(User.Role.EMPLOYER);
        user.setEnabled(false); // Pending admin approval
        user.setApproved(false);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT);
    }

    public List<User> getPendingEmployers() {
        return userRepository.findByRoleAndEnabled(User.Role.EMPLOYER, false);
    }

    public List<User> getAllEmployers() {
        return userRepository.findByRole(User.Role.EMPLOYER);
    }

    @Transactional
    public void approveEmployer(UUID userId) {
        User user = findById(userId);
        user.setEnabled(true);
        user.setApproved(true);
        userRepository.save(user);
        notificationService.sendNotification(user, "Account Approved!",
                "Your employer account has been approved. You can now post jobs.", "/employer/dashboard");
    }

    @Transactional
    public void rejectUser(UUID userId) {
        User user = findById(userId);
        user.setEnabled(false);
        user.setApproved(false);
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(UUID userId, String fullName, String phone) {
        User user = findById(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        userRepository.save(user);
    }
}
