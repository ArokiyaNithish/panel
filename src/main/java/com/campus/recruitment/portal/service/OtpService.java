package com.campus.recruitment.portal.service;

import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public void sendOtp(String email, String otp) {
        String subject = "Your OTP for Campus Recruitment Portal";
        String body = """
                <html><body style="font-family: Arial, sans-serif; background:#f4f4f4; padding:20px;">
                <div style="max-width:600px; margin:auto; background:#fff; border-radius:12px; padding:30px;">
                    <h2 style="color:#6c63ff;">Security Verification</h2>
                    <p>Hello,</p>
                    <p>Your one-time password (OTP) for verification is:</p>
                    <div style="background:#f0f0ff; color:#6c63ff; padding:20px; border-radius:8px; text-align:center; font-size:32px; font-weight:bold; letter-spacing:5px; margin:20px 0;">
                        %s
                    </div>
                    <p>This code is valid for <strong>5 minutes</strong>. If you did not request this code, please ignore this email.</p>
                    <p>Best regards,<br><strong>Campus Recruitment Team</strong></p>
                </div>
                </body></html>
                """.formatted(otp);
        
        emailService.sendEmail(email, subject, body);
        
        // Also update user if they exist (for login flow)
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setOtp(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);
        });
    }

    public boolean verifyOtp(User user, String enteredOtp) {
        if (user.getOtp() == null || user.getOtpExpiry() == null) return false;
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) return false;
        return user.getOtp().equals(enteredOtp);
    }
}
