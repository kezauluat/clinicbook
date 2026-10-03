package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.User;

import java.util.List;

public record UserResponse(Long id, String fullName, String email, String phone, String provider, List<String> roles) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getProvider().name(),
                u.getRoles().stream().map(r -> r.getName().name()).sorted().toList());
    }
}
