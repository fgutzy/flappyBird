package org.example.apiservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/*
DTO for user registration requests.
 */
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    public String username;

    @NotBlank
    @Size(min = 3, max = 50)
    public String password;
}

