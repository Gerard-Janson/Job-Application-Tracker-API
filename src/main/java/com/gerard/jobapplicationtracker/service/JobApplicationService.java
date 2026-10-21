package com.gerard.jobapplicationtracker.service;

import com.gerard.jobapplicationtracker.dtos.JobApplicationRequest;
import com.gerard.jobapplicationtracker.dtos.JobApplicationResponse;
import com.gerard.jobapplicationtracker.dtos.UpdateStatusRequest;
import com.gerard.jobapplicationtracker.entity.JobApplication;
import com.gerard.jobapplicationtracker.entity.User;
import com.gerard.jobapplicationtracker.enums.Status;
import com.gerard.jobapplicationtracker.mapper.JobApplicationMapper;
import com.gerard.jobapplicationtracker.repository.JobApplicationRepository;
import com.gerard.jobapplicationtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper mapper;

    public JobApplicationResponse createJobApplication(JobApplicationRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        JobApplication jobEntity = mapper.toEntity(request);
        jobEntity.setUser(user);
        if (jobEntity.getStatus() == null) {
            jobEntity.setStatus(Status.SAVED);
        }
        jobEntity = jobApplicationRepository.save(jobEntity);
        return mapper.toResponse(jobEntity);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> findAllByUserEmail(String email, Pageable pageable) {
        return jobApplicationRepository.findAllByUserEmail(email,pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getJobApplication(UUID id, String email) {
        return mapper.toResponse(findOwned(id, email));
    }

    public JobApplicationResponse updateJobStatus(UUID id, UpdateStatusRequest request, String email) {
        JobApplication application = findOwned(id, email);
        application.setStatus(request.status());
        return mapper.toResponse(jobApplicationRepository.save(application));
    }

    public void deleteJobApplication(UUID id, String email) {
        jobApplicationRepository.delete(findOwned(id, email));
    }

    public JobApplicationResponse updateJobApplication(UUID id, JobApplicationRequest request, String email) {
        JobApplication application = findOwned(id, email);
        mapper.updateEntity(request, application);
        return mapper.toResponse(jobApplicationRepository.save(application));
    }

    private JobApplication findOwned(UUID id, String email) {
        return jobApplicationRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job application not found"));
    }
}