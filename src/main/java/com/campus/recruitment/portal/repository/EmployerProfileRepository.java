package com.campus.recruitment.portal.repository;

import com.campus.recruitment.portal.model.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, UUID> {
    Optional<EmployerProfile> findByUserId(UUID userId);
}
