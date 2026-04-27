package com.campus.recruitment.portal.controller;

import com.campus.recruitment.portal.model.Notification;
import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.repository.NotificationRepository;
import com.campus.recruitment.portal.service.NotificationService;
import com.campus.recruitment.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/read/{id}")
    public String markAsRead(@PathVariable UUID id, Authentication auth) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        if (notifOpt.isPresent()) {
            Notification n = notifOpt.get();
            // Security check
            if (n.getUser().getEmail().equals(auth.getName())) {
                notificationService.markRead(id);
                if (n.getLink() != null && !n.getLink().isEmpty()) {
                    return "redirect:" + n.getLink();
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping("/mark-all-read")
    public String markAllRead(Authentication auth) {
        userService.findByEmail(auth.getName()).ifPresent(user -> {
            notificationService.markAllRead(user);
        });
        return "redirect:/";
    }
}
