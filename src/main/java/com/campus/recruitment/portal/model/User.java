package com.campus.recruitment.portal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class User extends BaseEntity {

    @NotNull
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String fullName;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = false; // Admin must approve Employer accounts

    @Column(nullable = false)
    private boolean approved = true; // For students auto-approved, Employers need admin approval

    @Column
    private String phone;

    @Column
    private String otp;

    @Column
    private LocalDateTime otpExpiry;

    @Column
    private LocalDateTime lastLoginAt;

    @Column
    private String lastLoginIp;

    public enum Role {
        STUDENT, EMPLOYER, ADMIN
    }
}
