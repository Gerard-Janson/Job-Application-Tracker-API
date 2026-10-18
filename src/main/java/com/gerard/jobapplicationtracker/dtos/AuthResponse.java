package com.gerard.jobapplicationtracker.dtos;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {}