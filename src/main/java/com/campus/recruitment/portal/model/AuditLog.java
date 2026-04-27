package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AuditLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column
    private String ipAddress;

    public AuditLog(User user, String action, String details, String ipAddress) {
        this.user = user;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
    }
}
