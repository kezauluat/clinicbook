package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.document.VisitNote;

import java.time.Instant;
import java.util.List;

public record VisitNoteResponse(String id, Long appointmentId, Long patientId, Long doctorId,
                                String notes, List<String> prescriptions, Instant createdAt) {
    public static VisitNoteResponse from(VisitNote n) {
        return new VisitNoteResponse(n.getId(), n.getAppointmentId(), n.getPatientId(), n.getDoctorId(),
                n.getNotes(), n.getPrescriptions(), n.getCreatedAt());
    }
}
