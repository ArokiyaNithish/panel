package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Application extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private Double aiMatchScore; // AI-computed match percentage

    private Integer assessmentScore; // Out of 5

    @Column(columnDefinition = "TEXT")
    private String employerNotes;

    @Column(updatable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    private LocalDateTime lastUpdated = LocalDateTime.now();

    public enum ApplicationStatus {
        PENDING, UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED
    }
}
