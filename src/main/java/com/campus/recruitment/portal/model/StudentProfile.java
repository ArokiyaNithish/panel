package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StudentProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String department;
    private String degree;
    private String year;
    private String rollNumber;
    private Double cgpa;

    @Column(columnDefinition = "TEXT")
    private String skills;           // AI-extracted: comma separated

    @Column(columnDefinition = "TEXT")
    private String technicalSkills;  // AI-extracted

    @Column(columnDefinition = "TEXT")
    private String projects;         // AI-extracted

    @Column(columnDefinition = "TEXT")
    private String experience;       // AI-extracted

    @Column(columnDefinition = "TEXT")
    private String certifications;   // AI-extracted

    @Column(columnDefinition = "TEXT")
    private String summary;          // AI-generated profile summary

    private String resumePath;       // Path to uploaded resume file

    private String linkedinUrl;
    private String githubUrl;

    @Column(columnDefinition = "TEXT")
    private String aiRecommendations; // AI job recommendations
}
