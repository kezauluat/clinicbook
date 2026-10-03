package rw.auca.clinicbook.dto;

import jakarta.validation.constraints.*;

public record CreateStaffRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "must be a valid phone number") String phone) {}
