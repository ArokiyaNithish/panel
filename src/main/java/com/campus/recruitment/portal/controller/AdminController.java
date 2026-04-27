package com.campus.recruitment.portal.controller;

import com.campus.recruitment.portal.model.*;
import com.campus.recruitment.portal.repository.*;
import com.campus.recruitment.portal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private AuditService auditService;
    @Autowired private AdminProfileRepository adminProfileRepository;

    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User admin = getCurrentUser(auth);
        long totalStudents = userRepository.findByRole(User.Role.STUDENT).size();
        long totalEmployers = userRepository.findByRole(User.Role.EMPLOYER).size();
        long pendingEmployers = userService.getPendingEmployers().size();
        long totalJobs = jobRepository.count();
        long activeJobs = jobRepository.countByStatus(Job.JobStatus.ACTIVE);
        long totalApplications = applicationRepository.count();
        long hiredCount = applicationRepository.countByStatus(Application.ApplicationStatus.SELECTED);

        model.addAttribute("admin", admin);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalEmployers", totalEmployers);
        model.addAttribute("pendingEmployers", pendingEmployers);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("hiredCount", hiredCount);
        model.addAttribute("pendingEmployersList", userService.getPendingEmployers());
        model.addAttribute("recentJobs", jobRepository.findAll().stream().limit(5).toList());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));

        // Chart Data
        model.addAttribute("jobStatus_ACTIVE", jobRepository.countByStatus(Job.JobStatus.ACTIVE));
        model.addAttribute("jobStatus_CLOSED", jobRepository.countByStatus(Job.JobStatus.CLOSED));
        model.addAttribute("jobStatus_DRAFT", jobRepository.countByStatus(Job.JobStatus.DRAFT));

        model.addAttribute("appStatus_PENDING", applicationRepository.countByStatus(Application.ApplicationStatus.PENDING));
        model.addAttribute("appStatus_SHORTLISTED", applicationRepository.countByStatus(Application.ApplicationStatus.SHORTLISTED));
        model.addAttribute("appStatus_SELECTED", applicationRepository.countByStatus(Application.ApplicationStatus.SELECTED));
        model.addAttribute("appStatus_REJECTED", applicationRepository.countByStatus(Application.ApplicationStatus.REJECTED));

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String allUsers(Authentication auth, Model model,
                           @RequestParam(defaultValue = "ALL") String role) {
        User admin = getCurrentUser(auth);
        List<User> users;
        if ("STUDENT".equals(role)) users = userRepository.findByRole(User.Role.STUDENT);
        else if ("EMPLOYER".equals(role)) users = userRepository.findByRole(User.Role.EMPLOYER);
        else users = userRepository.findAll();

        model.addAttribute("admin", admin);
        model.addAttribute("users", users);
        model.addAttribute("selectedRole", role);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));
        return "admin/users";
    }

    @PostMapping("/users/{id}/approve")
    public String approveUser(Authentication auth, @PathVariable UUID id, RedirectAttributes ra) {
        userService.approveEmployer(id);
        User admin = getCurrentUser(auth);
        User employer = userRepository.findById(id).orElse(null);
        auditService.log(admin, "APPROVE_EMPLOYER", "Approved employer account: " + (employer != null ? employer.getEmail() : id));
        ra.addFlashAttribute("success", "Employer approved successfully!");
        return "redirect:/admin/users?role=EMPLOYER";
    }

    @PostMapping("/users/{id}/reject")
    public String rejectUser(@PathVariable UUID id, RedirectAttributes ra) {
        userService.rejectUser(id);
        ra.addFlashAttribute("success", "User account disabled.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable UUID id, RedirectAttributes ra) {
        userRepository.deleteById(id);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }

    @GetMapping("/jobs")
    public String allJobs(Authentication auth, Model model) {
        User admin = getCurrentUser(auth);
        model.addAttribute("admin", admin);
        model.addAttribute("jobs", jobRepository.findAll());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));
        return "admin/jobs";
    }

    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable UUID id, RedirectAttributes ra) {
        jobRepository.deleteById(id);
        ra.addFlashAttribute("success", "Job removed.");
        return "redirect:/admin/jobs";
    }

    @GetMapping("/applications")
    public String allApplications(Authentication auth, Model model,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) UUID companyId) {
        User admin = getCurrentUser(auth);
        List<Application> applications;
        
        boolean hasStatus = status != null && !status.equals("ALL");
        boolean hasCompany = companyId != null;
        
        if (hasCompany) {
            User employer = userRepository.findById(companyId).orElse(null);
            if (employer != null) {
                if (hasStatus) {
                    applications = applicationRepository.findByEmployerAndStatus(employer, Application.ApplicationStatus.valueOf(status));
                } else {
                    applications = applicationRepository.findByEmployer(employer);
                }
            } else {
                applications = List.of();
            }
        } else {
            if (hasStatus) {
                applications = applicationRepository.findByStatus(Application.ApplicationStatus.valueOf(status));
            } else {
                applications = applicationRepository.findAll();
            }
        }

        List<User> employers = userRepository.findByRole(User.Role.EMPLOYER);
        model.addAttribute("employers", employers);
        model.addAttribute("selectedCompanyId", companyId);

        model.addAttribute("admin", admin);
        model.addAttribute("applications", applications);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));
        return "admin/applications";
    }

    @GetMapping("/logs")
    public String activityLogs(Authentication auth, Model model) {
        User admin = getCurrentUser(auth);
        model.addAttribute("admin", admin);
        model.addAttribute("logs", auditLogRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));
        return "admin/logs";
    }
}

