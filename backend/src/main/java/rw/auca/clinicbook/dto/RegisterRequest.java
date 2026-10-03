package rw.auca.clinicbook.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain letters and numbers")
        String password,
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "must be a valid phone number") String phone,
        @Past LocalDate dateOfBirth,
        @Pattern(regexp = "^(MALE|FEMALE)$", message = "must be MALE or FEMALE") String gender,
        @Pattern(regexp = "^[0-9]{16}$", message = "must be 16 digits") String nationalId
) {}
