package com.smartbus.booking.dto;

public record RegistrationRequest(
        String username,
        String fullName,
        String phone,
        String password,
        String email,
        String verificationCode
) {}
