package com.gerard.jobapplicationtracker.repository;

import com.gerard.jobapplicationtracker.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    List<JobApplication> findAllByUserEmail(String email);
    Optional<JobApplication> findByIdAndUserEmail(UUID id, String email);
}
