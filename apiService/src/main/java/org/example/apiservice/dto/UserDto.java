package org.example.apiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
DTO for user registration requests.
 */
public class UserDto {
    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    public String username;

    @NotBlank
    @Size(min = 3, max = 50)
    public String password;
}

