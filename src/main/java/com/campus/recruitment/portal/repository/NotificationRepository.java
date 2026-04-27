package com.campus.recruitment.portal.repository;

import com.campus.recruitment.portal.model.Notification;
import com.campus.recruitment.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsRead(User user, boolean isRead);
    long countByUserAndIsRead(User user, boolean isRead);
}
