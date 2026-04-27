package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assessment_questions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AssessmentQuestion extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private String optionA;
    
    @Column(nullable = false)
    private String optionB;
    
    @Column(nullable = false)
    private String optionC;
    
    @Column(nullable = false)
    private String optionD;

    @Column(nullable = false)
    private String correctOption; // A, B, C, or D
}
