package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.Patient;

import java.time.LocalDate;

public record PatientResponse(Long id, Long userId, String fullName, String email, String phone,
                              LocalDate dateOfBirth, String gender) {
    public static PatientResponse from(Patient p) {
        return new PatientResponse(p.getId(), p.getUser().getId(), p.getUser().getFullName(), p.getUser().getEmail(),
                p.getUser().getPhone(), p.getDateOfBirth(), p.getGender());
    }
}
