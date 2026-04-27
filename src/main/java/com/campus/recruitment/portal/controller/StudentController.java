package com.campus.recruitment.portal.controller;

import com.campus.recruitment.portal.model.*;
import com.campus.recruitment.portal.repository.*;
import com.campus.recruitment.portal.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private InterviewRepository interviewRepository;
    @Autowired private StudentProfileRepository profileRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private EmailService emailService;
    @Autowired private GeminiAIService geminiAIService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private AssessmentQuestionRepository assessmentQuestionRepository;
    @Autowired private AuditService auditService;

    private User getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElseThrow();
    }

    // ─── Dashboard ────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User student = getCurrentUser(auth);
        List<Application> applications = applicationRepository.findByStudent(student);
        List<Interview> interviews = interviewRepository.findByStudent(student);
        List<Notification> notifications = notificationService.getUserNotifications(student);
        StudentProfile profile = profileRepository.findByUserId(student.getId()).orElse(new StudentProfile());

        model.addAttribute("student", student);
        model.addAttribute("profile", profile);
        model.addAttribute("applications", applications);
        model.addAttribute("interviews", interviews);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));

        // Stats
        long pending = applications.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.PENDING).count();
        long shortlisted = applications.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.SHORTLISTED).count();
        long scheduled = applications.stream().filter(a -> a.getStatus() == Application.ApplicationStatus.INTERVIEW_SCHEDULED).count();
        model.addAttribute("pendingCount", pending);
        model.addAttribute("shortlistedCount", shortlisted);
        model.addAttribute("scheduledCount", scheduled);

        // Prepare Calendar Events
        List<Map<String, Object>> events = new ArrayList<>();
        for (Application app : applications) {
            if (app.getStatus() == Application.ApplicationStatus.SHORTLISTED) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "⭐ Shortlisted: " + app.getJob().getTitle());
                event.put("start", app.getAppliedAt().toLocalDate().toString());
                event.put("color", "#a78bfa");
                events.add(event);
            } else if (app.getStatus() == Application.ApplicationStatus.SELECTED) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "🏆 Selected: " + app.getJob().getTitle());
                event.put("start", app.getAppliedAt().toLocalDate().toString());
                event.put("color", "#22c55e");
                events.add(event);
            }
        }
        for (Interview iv : interviews) {
            Map<String, Object> event = new HashMap<>();
            event.put("title", "📅 Interview: " + iv.getApplication().getJob().getTitle());
            event.put("start", iv.getInterviewDate().toString());
            event.put("color", "#6c63ff");
            events.add(event);
        }
        model.addAttribute("calendarEvents", events);

        return "student/dashboard";
    }

    // ─── Profile ──────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profilePage(Authentication auth, Model model) {
        User student = getCurrentUser(auth);
        StudentProfile profile = profileRepository.findByUserId(student.getId()).orElse(new StudentProfile());
        model.addAttribute("student", student);
        model.addAttribute("profile", profile);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
        return "student/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String department,
                                @RequestParam String degree,
                                @RequestParam String year,
                                @RequestParam String rollNumber,
                                @RequestParam(required = false) Double cgpa,
                                @RequestParam(required = false) String skills,
                                @RequestParam(required = false) String linkedinUrl,
                                @RequestParam(required = false) String githubUrl,
                                RedirectAttributes ra) {
        User student = getCurrentUser(auth);
        userService.updateProfile(student.getId(), fullName, phone);

        StudentProfile profile = profileRepository.findByUserId(student.getId()).orElse(new StudentProfile());
        profile.setUser(student);
        profile.setDepartment(department);
        profile.setDegree(degree);
        profile.setYear(year);
        profile.setRollNumber(rollNumber);
        profile.setCgpa(cgpa);
        profile.setSkills(skills);
        profile.setLinkedinUrl(linkedinUrl);
        profile.setGithubUrl(githubUrl);
        profileRepository.save(profile);
        auditService.log(student, "UPDATE_PROFILE", "Updated personal and academic details");

        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/student/profile";
    }

    @PostMapping("/profile/upload-resume")
    public String uploadResume(Authentication auth,
                               @RequestParam("resume") MultipartFile file,
                               RedirectAttributes ra) throws IOException {
        User student = getCurrentUser(auth);
        if (file.isEmpty() || !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".pdf")) {
            ra.addFlashAttribute("error", "Please upload a valid PDF resume.");
            return "redirect:/student/profile";
        }

        String filename = fileStorageService.storeFile(file);
        StudentProfile profile = profileRepository.findByUserId(student.getId()).orElse(new StudentProfile());
        profile.setUser(student);

        // AI Resume Parsing
        try {
            String resumeText = new String(file.getBytes());
            String parsed = geminiAIService.parseResume(resumeText);
            if (parsed != null) {
                ObjectMapper mapper = new ObjectMapper();
                // Strip markdown code fences if present
                parsed = parsed.replaceAll("```json", "").replaceAll("```", "").trim();
                JsonNode node = mapper.readTree(parsed);
                profile.setSkills(node.path("skills").asText(""));
                profile.setTechnicalSkills(node.path("technicalSkills").asText(""));
                profile.setExperience(node.path("experience").asText(""));
                profile.setProjects(node.path("projects").asText(""));
                profile.setCertifications(node.path("certifications").asText(""));
                profile.setSummary(node.path("summary").asText(""));
            }
        } catch (Exception e) {
            // AI parsing failed, still save resume
        }

        profile.setResumePath(filename);
        profileRepository.save(profile);
        notificationService.sendNotification(student, "Resume Uploaded",
                "Your resume was uploaded and parsed by AI.", "/student/profile");
        ra.addFlashAttribute("success", "Resume uploaded and parsed successfully!");
        return "redirect:/student/profile";
    }

    // ─── Job Search ───────────────────────────────────────────────
    @GetMapping("/jobs")
    public String jobSearch(Authentication auth,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String location,
                            Model model) {
        User student = getCurrentUser(auth);
        List<Job> jobs;
        if (keyword != null && !keyword.isBlank()) {
            jobs = jobRepository.searchJobs(keyword);
        } else if (category != null || location != null) {
            jobs = jobRepository.filterJobs(
                    (category != null && !category.isBlank()) ? category : null,
                    (location != null && !location.isBlank()) ? location : null);
        } else {
            jobs = jobRepository.findByStatus(Job.JobStatus.ACTIVE);
        }

        // Mark applied jobs
        Set<UUID> appliedJobIds = new HashSet<>();
        applicationRepository.findByStudent(student).forEach(a -> appliedJobIds.add(a.getJob().getId()));

        model.addAttribute("student", student);
        model.addAttribute("jobs", jobs);
        model.addAttribute("appliedJobIds", appliedJobIds);
        model.addAttribute("keyword", keyword);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
        return "student/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String jobDetail(Authentication auth, @PathVariable UUID id, Model model) {
        User student = getCurrentUser(auth);
        Job job = jobRepository.findById(id).orElseThrow();
        boolean alreadyApplied = applicationRepository.existsByStudentAndJob(student, job);

        // AI match score
        StudentProfile profile = profileRepository.findByUserId(student.getId()).orElse(new StudentProfile());
        String matchJson = geminiAIService.computeJobMatch(
                profile.getSkills() != null ? profile.getSkills() : "",
                job.getDescription(), job.getSkillsRequired() != null ? job.getSkillsRequired() : "");

        model.addAttribute("student", student);
        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("matchJson", matchJson);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
        return "student/job-detail";
    }

    // ─── Apply ────────────────────────────────────────────────────
    @PostMapping("/jobs/{id}/apply")
    public String applyJob(Authentication auth, @PathVariable UUID id,
                           @RequestParam(required = false) String coverLetter,
                           RedirectAttributes ra) {
        User student = getCurrentUser(auth);
        Job job = jobRepository.findById(id).orElseThrow();

        if (applicationRepository.existsByStudentAndJob(student, job)) {
            ra.addFlashAttribute("error", "You have already applied for this job.");
            return "redirect:/student/jobs/" + id;
        }

        StudentProfile profile = profileRepository.findByUserId(student.getId()).orElse(null);
        double matchScore = 50.0;
        if (profile != null) {
            try {
                String matchJson = geminiAIService.computeJobMatch(
                        profile.getSkills() != null ? profile.getSkills() : "",
                        job.getDescription(), job.getSkillsRequired() != null ? job.getSkillsRequired() : "");
                matchJson = matchJson.replaceAll("```json", "").replaceAll("```", "").trim();
                ObjectMapper mapper = new ObjectMapper();
                matchScore = mapper.readTree(matchJson).path("score").asDouble(50);
            } catch (Exception ignored) {}
        }

        Application application = new Application();
        application.setStudent(student);
        application.setJob(job);
        application.setCoverLetter(coverLetter);
        application.setAiMatchScore(matchScore);
        applicationRepository.save(application);

        notificationService.sendNotification(student, "Application Submitted",
                "You applied for " + job.getTitle() + " at " + job.getEmployer().getFullName(), "/student/applications");
        notificationService.sendNotification(job.getEmployer(), "New Application",
                student.getFullName() + " applied for " + job.getTitle(), "/employer/jobs/" + job.getId() + "/applicants");

        ra.addFlashAttribute("success", "Application submitted successfully!");
        return "redirect:/student/applications";
    }

    // ─── Applications ─────────────────────────────────────────────
    @GetMapping("/applications")
    public String myApplications(Authentication auth, Model model) {
        User student = getCurrentUser(auth);
        List<Application> applications = applicationRepository.findByStudent(student);
        List<Interview> interviews = interviewRepository.findByStudent(student);
        model.addAttribute("student", student);
        model.addAttribute("applications", applications);
        model.addAttribute("interviews", interviews);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
        return "student/applications";
    }

    @GetMapping("/applications/{id}/assessment")
    public String takeAssessment(Authentication auth, @PathVariable UUID id, Model model, RedirectAttributes ra) {
        User student = getCurrentUser(auth);
        Application app = applicationRepository.findById(id).orElseThrow();
        if (!app.getStudent().getId().equals(student.getId())) return "redirect:/student/applications";
        
        if (app.getStatus() != Application.ApplicationStatus.SHORTLISTED) {
            ra.addFlashAttribute("error", "Assessment is only available for shortlisted applications.");
            return "redirect:/student/applications";
        }
        
        if (app.getAssessmentScore() != null) {
            ra.addFlashAttribute("error", "You have already taken this assessment.");
            return "redirect:/student/applications";
        }
        
        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByJob(app.getJob());
        if (questions.isEmpty()) {
            ra.addFlashAttribute("error", "No assessment configured for this job.");
            return "redirect:/student/applications";
        }
        
        model.addAttribute("student", student);
        model.addAttribute("application", app);
        model.addAttribute("questions", questions);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
        return "student/assessment";
    }

    @PostMapping("/applications/{id}/assessment/submit")
    public String submitAssessment(Authentication auth, @PathVariable UUID id, 
                                   jakarta.servlet.http.HttpServletRequest request, 
                                   RedirectAttributes ra) {
        User student = getCurrentUser(auth);
        Application app = applicationRepository.findById(id).orElseThrow();
        if (!app.getStudent().getId().equals(student.getId())) return "redirect:/student/applications";
        
        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByJob(app.getJob());
        int score = 0;
        
        for (AssessmentQuestion q : questions) {
            String answer = request.getParameter("q_" + q.getId());
            if (answer != null && answer.equals(q.getCorrectOption())) {
                score++;
            }
        }
        
        app.setAssessmentScore(score);
        if (score >= 3) {
            app.setStatus(Application.ApplicationStatus.INTERVIEW_SCHEDULED);
            app.setEmployerNotes((app.getEmployerNotes() != null ? app.getEmployerNotes() + "\n" : "") + "Passed assessment with score " + score + "/5.");
            notificationService.sendNotification(student, "Assessment Passed! 🎉", "You scored " + score + "/5. You are automatically moved to the interview round.", "/student/applications");
        } else {
            app.setStatus(Application.ApplicationStatus.REJECTED);
            app.setEmployerNotes((app.getEmployerNotes() != null ? app.getEmployerNotes() + "\n" : "") + "Failed assessment with score " + score + "/5.");
            notificationService.sendNotification(student, "Assessment Failed", "You scored " + score + "/5. Unfortunately, you did not pass the assessment.", "/student/applications");
        }
        applicationRepository.save(app);
        auditService.log(student, "APPLY_JOB", "Applied for job: " + app.getJob().getTitle());
        
        ra.addFlashAttribute("success", "Assessment submitted successfully. Score: " + score + "/5.");
        return "redirect:/student/applications";
    }

    // ─── Notifications ────────────────────────────────────────────
    @PostMapping("/notifications/mark-read")
    @ResponseBody
    public String markNotificationsRead(Authentication auth) {
        User student = getCurrentUser(auth);
        notificationService.markAllRead(student);
        return "ok";
    }
}
