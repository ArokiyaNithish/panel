package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AdminProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private String department;

    @Column
    private String adminLevel; // e.g. SuperAdmin, Manager
}
