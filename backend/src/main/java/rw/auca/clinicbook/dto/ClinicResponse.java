package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.Clinic;

import java.io.Serializable;

public record ClinicResponse(Long id, String name, String address, String phone) implements Serializable {
    public static ClinicResponse from(Clinic c) {
        return new ClinicResponse(c.getId(), c.getName(), c.getAddress(), c.getPhone());
    }
}
