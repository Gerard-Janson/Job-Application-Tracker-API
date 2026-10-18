package com.gerard.jobapplicationtracker.controller;

import com.gerard.jobapplicationtracker.dtos.JobApplicationRequest;
import com.gerard.jobapplicationtracker.dtos.JobApplicationResponse;
import com.gerard.jobapplicationtracker.dtos.UpdateStatusRequest;
import com.gerard.jobapplicationtracker.service.JobApplicationService;
import com.gerard.jobapplicationtracker.entity.JobApplication;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracker")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/")
    public List<JobApplicationResponse> getAllJobApplicationsByUserEmail(@AuthenticationPrincipal UserDetails principal) {
        List<JobApplicationResponse> allApplications = jobApplicationService.findAllByUserEmail(principal.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(allApplications).getBody();
    }

    @PostMapping("/")
    public ResponseEntity<JobApplicationResponse> createJobApplication(@Valid @RequestBody JobApplicationRequest request, @AuthenticationPrincipal UserDetails principal) {
        JobApplicationResponse response= jobApplicationService.createJobApplication(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobApplicationResponse> updateJobStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        JobApplicationResponse response =
                jobApplicationService.updateJobStatus(id, request, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        jobApplicationService.deleteJobApplication(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getJobApplication(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        JobApplicationResponse response = jobApplicationService.getJobApplication(id, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateJobApplication(
            @PathVariable UUID id,
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                jobApplicationService.updateJobApplication(id, request, principal.getUsername()));
    }




}
