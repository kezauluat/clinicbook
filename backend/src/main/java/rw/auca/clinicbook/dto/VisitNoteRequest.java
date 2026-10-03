package rw.auca.clinicbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VisitNoteRequest(@NotBlank @Size(max = 5000) String notes, List<@NotBlank String> prescriptions) {}
