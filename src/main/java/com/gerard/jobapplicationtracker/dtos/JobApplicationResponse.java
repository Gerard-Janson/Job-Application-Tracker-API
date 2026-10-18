package com.gerard.jobapplicationtracker.dtos;

import com.gerard.jobapplicationtracker.enums.Job_type;
import com.gerard.jobapplicationtracker.enums.Status;
import com.gerard.jobapplicationtracker.enums.Work_mode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record JobApplicationResponse(
        UUID id,
        String position,
        String company,
        String jobDescription,
        String location,
        Job_type jobType,
        Work_mode workNode,
        BigDecimal salary,
        String post_url,
        Status status,
        LocalDate appliedDate,
        LocalDate nextFollowUpDate,
        LocalDate createdAt,
        LocalDate updatedAt

) {

}
