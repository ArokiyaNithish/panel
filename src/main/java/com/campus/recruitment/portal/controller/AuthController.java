package com.campus.recruitment.portal.controller;

import com.campus.recruitment.portal.service.UserService;
import com.campus.recruitment.portal.service.OtpService;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private com.campus.recruitment.portal.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private jakarta.servlet.http.HttpSession session;

    @Autowired
    private com.campus.recruitment.portal.service.AuditService auditService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid email, password, OTP, or CAPTCHA.");
        if (logout != null) model.addAttribute("message", "Logged out successfully.");
        
        // Generate CAPTCHA
        int num1 = (int) (Math.random() * 10) + 1;
        int num2 = (int) (Math.random() * 10) + 1;
        session.setAttribute("captchaAnswer", String.valueOf(num1 + num2));
        model.addAttribute("captchaQuestion", num1 + " + " + num2 + " = ?");
        
        return "auth/login";
    }

    @PostMapping("/login")
    public String doPreLogin(@RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String captcha,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             jakarta.servlet.http.HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response) {
        
        String expectedCaptcha = (String) session.getAttribute("captchaAnswer");
        if (expectedCaptcha == null || !expectedCaptcha.equals(captcha)) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/auth/login";
        }
        
        var userOpt = userService.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            // Direct login without OTP
            var user = userOpt.get();
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                new com.campus.recruitment.portal.security.CustomUserDetailsService(userRepository).loadUserByUsername(email);
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            
            new org.springframework.security.web.context.HttpSessionSecurityContextRepository().saveContext(
                org.springframework.security.core.context.SecurityContextHolder.getContext(), request, response);
            
            // Set session time
            session.setAttribute("loginTime", java.time.LocalDateTime.now());
            session.setAttribute("sessionEndTime", java.time.LocalDateTime.now().plusMinutes(10));
            auditService.log(user, "LOGIN", "User logged in via direct password match");
            
            if (user.getRole() == com.campus.recruitment.portal.model.User.Role.STUDENT) return "redirect:/student/dashboard";
            if (user.getRole() == com.campus.recruitment.portal.model.User.Role.EMPLOYER) return "redirect:/employer/dashboard";
            if (user.getRole() == com.campus.recruitment.portal.model.User.Role.ADMIN) return "redirect:/admin/dashboard";
            return "redirect:/";
        }
        redirectAttributes.addAttribute("error", "true");
        return "redirect:/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("roles", new String[]{"STUDENT", "EMPLOYER"});
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String phone,
                             @RequestParam String role,
                             RedirectAttributes redirectAttributes) {
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already registered.");
            return "redirect:/auth/register";
        }

        // Store registration data in session
        session.setAttribute("regFullName", fullName);
        session.setAttribute("regEmail", email);
        session.setAttribute("regPassword", password);
        session.setAttribute("regPhone", phone);
        session.setAttribute("regRole", role);

        String otp = otpService.generateOtp();
        session.setAttribute("regOtp", otp);
        session.setAttribute("regOtpExpiry", java.time.LocalDateTime.now().plusMinutes(5));

        otpService.sendOtp(email, otp);

        redirectAttributes.addAttribute("email", email);
        redirectAttributes.addAttribute("type", "register");
        return "redirect:/auth/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam String email, @RequestParam String type, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("type", type);
        return "auth/otp-verify";
    }

    @PostMapping("/verify-otp")
    public String doVerifyOtp(@RequestParam String email,
                              @RequestParam String type,
                              @RequestParam String otp,
                              RedirectAttributes redirectAttributes,
                              jakarta.servlet.http.HttpServletRequest request,
                              jakarta.servlet.http.HttpServletResponse response) {
        if ("register".equals(type)) {
            String sessionOtp = (String) session.getAttribute("regOtp");
            java.time.LocalDateTime expiry = (java.time.LocalDateTime) session.getAttribute("regOtpExpiry");

            if (sessionOtp != null && sessionOtp.equals(otp) && expiry.isAfter(java.time.LocalDateTime.now())) {
                String fullName = (String) session.getAttribute("regFullName");
                String password = (String) session.getAttribute("regPassword");
                String phone = (String) session.getAttribute("regPhone");
                String role = (String) session.getAttribute("regRole");

                if ("STUDENT".equals(role)) {
                    userService.registerStudent(fullName, email, password, phone);
                } else {
                    userService.registerEmployer(fullName, email, password, phone);
                }
                
                var newUser = userRepository.findByEmail(email).orElse(null);
                auditService.log(newUser, "REGISTER", "User registered and verified via OTP");
                
                // Programmatically authenticate after registration
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                    new com.campus.recruitment.portal.security.CustomUserDetailsService(userRepository).loadUserByUsername(email);
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                
                new org.springframework.security.web.context.HttpSessionSecurityContextRepository().saveContext(
                    org.springframework.security.core.context.SecurityContextHolder.getContext(), request, response);
                
                // Set session time
                session.setAttribute("loginTime", java.time.LocalDateTime.now());
                session.setAttribute("sessionEndTime", java.time.LocalDateTime.now().plusMinutes(10));
                
                if ("STUDENT".equals(role)) return "redirect:/student/dashboard";
                else return "redirect:/employer/dashboard";
            }
        } else if ("login".equals(type)) {
            var userOpt = userService.findByEmail(email);
            if (userOpt.isPresent() && otpService.verifyOtp(userOpt.get(), otp)) {
                // Clear OTP
                var user = userOpt.get();
                user.setOtp(null);
                user.setOtpExpiry(null);
                userRepository.save(user);

                // Programmatically authenticate
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                    new com.campus.recruitment.portal.security.CustomUserDetailsService(userRepository).loadUserByUsername(email);
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                
                // Set session time
                session.setAttribute("loginTime", java.time.LocalDateTime.now());
                session.setAttribute("sessionEndTime", java.time.LocalDateTime.now().plusMinutes(10));
                
                if (user.getRole() == com.campus.recruitment.portal.model.User.Role.STUDENT) return "redirect:/student/dashboard";
                if (user.getRole() == com.campus.recruitment.portal.model.User.Role.EMPLOYER) return "redirect:/employer/dashboard";
                if (user.getRole() == com.campus.recruitment.portal.model.User.Role.ADMIN) return "redirect:/admin/dashboard";
            }
        }
        
        redirectAttributes.addAttribute("email", email);
        redirectAttributes.addAttribute("type", type);
        redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP.");
        return "redirect:/auth/verify-otp";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email, @RequestParam String type, RedirectAttributes ra) {
        String otp = otpService.generateOtp();
        if ("register".equals(type)) {
            session.setAttribute("regOtp", otp);
            session.setAttribute("regOtpExpiry", java.time.LocalDateTime.now().plusMinutes(5));
        }
        otpService.sendOtp(email, otp);
        ra.addAttribute("email", email);
        ra.addAttribute("type", type);
        ra.addFlashAttribute("message", "A new OTP has been sent to your email.");
        return "redirect:/auth/verify-otp";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}
