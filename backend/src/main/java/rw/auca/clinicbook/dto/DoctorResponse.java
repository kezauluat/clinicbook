package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.Doctor;

import java.io.Serializable;

public record DoctorResponse(Long id, Long userId, String fullName, String email, String phone,
                             String specialization, String licenseNo,
                             Long departmentId, String departmentName, String clinicName) implements Serializable {
    public static DoctorResponse from(Doctor d) {
        return new DoctorResponse(d.getId(), d.getUser().getId(), d.getUser().getFullName(), d.getUser().getEmail(),
                d.getUser().getPhone(), d.getSpecialization(), d.getLicenseNo(),
                d.getDepartment().getId(), d.getDepartment().getName(), d.getDepartment().getClinic().getName());
    }
}
