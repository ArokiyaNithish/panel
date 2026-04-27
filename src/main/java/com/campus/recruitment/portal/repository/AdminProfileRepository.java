package com.campus.recruitment.portal.repository;

import com.campus.recruitment.portal.model.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, UUID> {
    Optional<AdminProfile> findByUserId(UUID userId);
}
