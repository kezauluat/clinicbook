package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.Appointment;

import java.time.LocalDateTime;

public record AppointmentResponse(Long id, Long patientId, String patientName, Long doctorId, String doctorName,
                                  String departmentName, LocalDateTime startAt, LocalDateTime endAt,
                                  String status, String reason) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(),
                a.getPatient().getId(), a.getPatient().getUser().getFullName(),
                a.getDoctor().getId(), a.getDoctor().getUser().getFullName(),
                a.getDoctor().getDepartment().getName(),
                a.getStartAt(), a.getEndAt(), a.getStatus().name(), a.getReason());
    }
}
