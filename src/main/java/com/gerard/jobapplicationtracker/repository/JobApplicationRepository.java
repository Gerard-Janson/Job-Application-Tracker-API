package com.gerard.jobapplicationtracker.repository;

import com.gerard.jobapplicationtracker.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    List<JobApplication> findAllByUserEmail(String email);
    Page<JobApplication> findAllByUserEmail(String email, Pageable pageable);
    Optional<JobApplication> findByIdAndUserEmail(UUID id, String email);
}
