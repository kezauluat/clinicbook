package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.Department;

import java.io.Serializable;

public record DepartmentResponse(Long id, String name, Long clinicId, String clinicName) implements Serializable {
    public static DepartmentResponse from(Department d) {
        return new DepartmentResponse(d.getId(), d.getName(), d.getClinic().getId(), d.getClinic().getName());
    }
}
