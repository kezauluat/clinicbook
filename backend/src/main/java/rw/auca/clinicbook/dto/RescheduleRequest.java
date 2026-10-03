package rw.auca.clinicbook.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RescheduleRequest(@NotNull @Future LocalDateTime startAt) {}
