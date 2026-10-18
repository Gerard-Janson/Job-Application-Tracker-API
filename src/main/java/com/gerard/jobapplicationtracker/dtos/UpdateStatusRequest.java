package com.gerard.jobapplicationtracker.dtos;

import com.gerard.jobapplicationtracker.enums.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStatusRequest(
        @NotNull Status status,
        @Size(max = 500) String note
) {
}