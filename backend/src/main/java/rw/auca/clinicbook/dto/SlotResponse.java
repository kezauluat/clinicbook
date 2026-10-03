package rw.auca.clinicbook.dto;

import java.time.LocalDateTime;

public record SlotResponse(LocalDateTime startAt, LocalDateTime endAt) {}
