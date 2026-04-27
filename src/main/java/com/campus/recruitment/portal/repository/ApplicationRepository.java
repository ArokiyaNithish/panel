package com.campus.recruitment.portal.repository;

import com.campus.recruitment.portal.model.Application;
import com.campus.recruitment.portal.model.Job;
import com.campus.recruitment.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByStudent(User student);
    List<Application> findByJob(Job job);
    List<Application> findByStudentAndStatus(User student, Application.ApplicationStatus status);
    Optional<Application> findByStudentAndJob(User student, Job job);
    boolean existsByStudentAndJob(User student, Job job);
    List<Application> findByStatus(Application.ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.job.employer = :employer ORDER BY a.appliedAt DESC")
    List<Application> findByEmployer(@Param("employer") User employer);

    @Query("SELECT a FROM Application a WHERE a.job.employer = :employer AND a.status = :status")
    List<Application> findByEmployerAndStatus(@Param("employer") User employer,
                                               @Param("status") Application.ApplicationStatus status);

    long countByStatus(Application.ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.job.employer = :employer AND a.job.id = :jobId")
    List<Application> findByEmployerAndJobId(@Param("employer") User employer,
                                              @Param("jobId") UUID jobId);
}
