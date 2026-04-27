package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employer_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EmployerProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private String companyName;

    @Column
    private String website;

    @Column
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String logoPath;
    
    @Column
    private String address;
}
