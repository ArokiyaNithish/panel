package com.campus.recruitment.portal.config;

import com.campus.recruitment.portal.model.User;
import com.campus.recruitment.portal.service.NotificationService;
import com.campus.recruitment.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            userService.findByEmail(auth.getName()).ifPresent(user -> {
                model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
                model.addAttribute("notifications", notificationService.getUserNotifications(user));
            });
        }
    }
}
