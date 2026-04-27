package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "interviews")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Interview extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    private LocalDate interviewDate;
    private LocalTime interviewTime;
    private String slot;         // e.g., "10:00 AM - 10:30 AM"
    private String venue;        // e.g., "Room 301 / Google Meet"
    private String meetLink;
    private String interviewerName;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean reminderSent = false;

    public enum InterviewStatus {
        SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
    }
}
