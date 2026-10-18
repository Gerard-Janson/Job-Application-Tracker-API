package com.gerard.jobapplicationtracker.mapper;

import com.gerard.jobapplicationtracker.dtos.JobApplicationRequest;
import com.gerard.jobapplicationtracker.dtos.JobApplicationResponse;
import com.gerard.jobapplicationtracker.entity.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {
    public JobApplicationResponse toResponse(JobApplication a) {
        return new JobApplicationResponse(
                a.getId(),
                a.getPosition(),
                a.getCompany(),
                a.getJob_description(),
                a.getLocation(),
                a.getJob_type(),
                a.getWork_mode(),
                a.getSalary(),
                a.getPost_url(),
                a.getStatus(),
                a.getAppliedDate(),
                a.getNextFollowUpDate(),
                a.getCreatedAt(),
                a.getUpdatedAt());
    }

    public JobApplication toEntity(JobApplicationRequest request) {
        JobApplication jobApplication = new JobApplication();
        jobApplication.setPosition(request.position());
        jobApplication.setCompany(request.company());
        jobApplication.setJob_description(request.jobDescription());
        jobApplication.setLocation(request.location());
        jobApplication.setJob_type(request.jobType());
        jobApplication.setWork_mode(request.workMode());
        jobApplication.setSalary(request.salary());
        jobApplication.setPost_url(request.postUrl());
        jobApplication.setStatus(request.status());
        jobApplication.setAppliedDate(request.appliedDate());
        jobApplication.setNextFollowUpDate(request.nextFollowUpDate());
        return jobApplication;
    }

    public void updateEntity(JobApplicationRequest request, JobApplication application) {
        application.setPosition(request.position());
        application.setCompany(request.company());
        application.setJob_description(request.jobDescription());
        application.setLocation(request.location());
        application.setJob_type(request.jobType());
        application.setWork_mode(request.workMode());
        application.setSalary(request.salary());
        application.setPost_url(request.postUrl());
        application.setAppliedDate(request.appliedDate());
        application.setNextFollowUpDate(request.nextFollowUpDate());
        if (request.status() != null) {
            application.setStatus(request.status());
        }
    }

    


}
