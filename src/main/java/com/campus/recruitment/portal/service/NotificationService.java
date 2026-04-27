package com.campus.recruitment.portal.service;

import com.campus.recruitment.portal.model.Notification;
import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private com.campus.recruitment.portal.repository.UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @org.springframework.scheduling.annotation.Async
    public void sendJobAlertToAllStudents(com.campus.recruitment.portal.model.Job job) {
        List<User> students = userRepository.findByRole(User.Role.STUDENT);
        String title = "New Job Alert: " + job.getEmployer().getFullName() + " is hiring!";
        String message = "Role: " + job.getTitle() + " | Deadline: " + (job.getDeadline() != null ? job.getDeadline().toLocalDate() : "TBA");
        
        for (User student : students) {
            // In-app notification
            Notification notification = new Notification();
            notification.setUser(student);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setLink("/student/jobs/" + job.getId());
            notificationRepository.save(notification);
            
            // Email alert
            emailService.sendNewJobAlertEmail(student.getEmail(), student.getFullName(), job.getTitle(), job.getEmployer().getFullName(), job.getLocation(), job.getDeadline() != null ? job.getDeadline().toLocalDate().toString() : "TBA");
        }
    }

    public void sendNotification(User user, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    public void markAllRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsRead(user, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public void markRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
