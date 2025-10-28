package org.example.apiservice.dto;

import jakarta.validation.constraints.*;

/*
DTO for score submission requests.
 */
public class ScoreRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    public String username;

    @NotBlank
    @Size(min = 3, max = 50)
    public String password;

    @Min(0)
    @Max(999999)
    public int score;
}
