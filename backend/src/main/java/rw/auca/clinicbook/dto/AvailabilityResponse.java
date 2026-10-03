package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.Availability;

import java.time.LocalTime;

public record AvailabilityResponse(Long id, Integer dayOfWeek, LocalTime startTime, LocalTime endTime) {
    public static AvailabilityResponse from(Availability a) {
        return new AvailabilityResponse(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime());
    }
}
