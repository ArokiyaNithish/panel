package com.campus.recruitment.portal.repository;

import com.campus.recruitment.portal.model.Job;
import com.campus.recruitment.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByEmployer(User employer);
    List<Job> findByStatus(Job.JobStatus status);
    List<Job> findByEmployerAndStatus(User employer, Job.JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           "LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    List<Job> searchJobs(@Param("keyword") String keyword);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(:category IS NULL OR j.category = :category) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%',:location,'%')))")
    List<Job> filterJobs(@Param("category") String category,
                         @Param("location") String location);

    long countByStatus(Job.JobStatus status);
}
