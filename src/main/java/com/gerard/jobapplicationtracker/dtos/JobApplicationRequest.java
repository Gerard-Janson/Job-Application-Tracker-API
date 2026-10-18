package com.gerard.jobapplicationtracker.dtos;

import com.gerard.jobapplicationtracker.enums.Job_type;
import com.gerard.jobapplicationtracker.enums.Status;
import com.gerard.jobapplicationtracker.enums.Work_mode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JobApplicationRequest(
        @NotBlank @Size(max = 255) String position,
        @NotBlank @Size(max = 500) String company,
        @Size(max = 255) String jobDescription,
        @Size(max = 500) String location,
        @NotNull Job_type jobType,
        @NotNull Work_mode workMode,
        @Positive BigDecimal salary,
        @Size(max = 500) String postUrl,
        Status status,
        @PastOrPresent LocalDate appliedDate,
        @FutureOrPresent LocalDate nextFollowUpDate
) {
}