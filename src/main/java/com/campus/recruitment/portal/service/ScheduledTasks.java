package com.campus.recruitment.portal.service;

import com.campus.recruitment.portal.model.Interview;
import com.campus.recruitment.portal.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class ScheduledTasks {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private EmailService emailService;

    // Run every minute (60000 ms)
    @Scheduled(fixedRate = 60000)
    public void sendInterviewReminders() {
        // Find all SCHEDULED interviews that haven't had a reminder sent yet
        List<Interview> upcomingInterviews = interviewRepository.findUpcomingReminders(Interview.InterviewStatus.SCHEDULED);
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Interview interview : upcomingInterviews) {
            if (interview.getInterviewDate() != null && interview.getInterviewTime() != null) {
                LocalDateTime interviewDateTime = LocalDateTime.of(interview.getInterviewDate(), interview.getInterviewTime());
                
                // If the interview is exactly 1 hour (60 minutes) or less away, AND it is still in the future
                if (now.plusHours(1).isAfter(interviewDateTime) && now.isBefore(interviewDateTime)) {
                    
                    // Send Email
                    String to = interview.getApplication().getStudent().getEmail();
                    String studentName = interview.getApplication().getStudent().getFullName();
                    String jobTitle = interview.getApplication().getJob().getTitle();
                    String companyName = interview.getApplication().getJob().getEmployer().getFullName();
                    
                    String formattedTime = interview.getInterviewTime().toString();
                    String venue = interview.getVenue() != null && !interview.getVenue().isBlank() ? interview.getVenue() : interview.getMeetLink();
                    if (venue == null || venue.isBlank()) venue = "TBA";
                    
                    String interviewer = interview.getInterviewerName() != null ? interview.getInterviewerName() : "Hiring Manager";
                    
                    try {
                        emailService.sendInterviewReminderEmail(to, studentName, jobTitle, companyName, formattedTime, venue, interviewer);
                        
                        // Mark reminder as sent
                        interview.setReminderSent(true);
                        interviewRepository.save(interview);
                        System.out.println("Reminder sent for interview ID: " + interview.getId());
                    } catch (Exception e) {
                        System.err.println("Failed to send reminder for interview " + interview.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}
