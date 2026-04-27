package com.campus.recruitment.portal.repository;

import com.campus.recruitment.portal.model.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    Optional<Interview> findByApplicationId(UUID applicationId);
    
    @Query("SELECT i FROM Interview i WHERE i.status = :status AND (i.reminderSent = false OR i.reminderSent IS NULL)")
    List<Interview> findUpcomingReminders(@Param("status") Interview.InterviewStatus status);

    @Query("SELECT i FROM Interview i WHERE i.application.student = :student ORDER BY i.interviewDate ASC")
    List<Interview> findByStudent(@Param("student") com.campus.recruitment.portal.model.User student);

    @Query("SELECT i FROM Interview i WHERE i.application.job.employer = :employer ORDER BY i.interviewDate ASC")
    List<Interview> findByEmployer(@Param("employer") com.campus.recruitment.portal.model.User employer);

    @Query("SELECT i FROM Interview i WHERE i.interviewDate = :date")
    List<Interview> findByDate(@Param("date") LocalDate date);

    @Query("SELECT i FROM Interview i WHERE i.application.job.employer = :employer AND i.interviewDate >= :fromDate ORDER BY i.interviewDate ASC")
    List<Interview> findUpcomingByEmployer(@Param("employer") com.campus.recruitment.portal.model.User employer,
                                            @Param("fromDate") LocalDate fromDate);
}
