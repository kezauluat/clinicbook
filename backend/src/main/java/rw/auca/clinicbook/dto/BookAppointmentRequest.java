package rw.auca.clinicbook.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** patientId is only used when a receptionist books for a walk-in patient. */
public record BookAppointmentRequest(
        @NotNull Long doctorId,
        @NotNull @Future LocalDateTime startAt,
        @Size(max = 255) String reason,
        Long patientId) {}
