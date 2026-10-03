package rw.auca.clinicbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClinicRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String address,
        @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "must be a valid phone number") String phone) {}
