package rw.auca.clinicbook.dto;

import rw.auca.clinicbook.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(Long id, String fullName, String email, String phone, String provider,
                                List<String> roles, boolean enabled, LocalDateTime createdAt) {
    public static AdminUserResponse from(User u) {
        return new AdminUserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getProvider().name(),
                u.getRoles().stream().map(r -> r.getName().name()).sorted().toList(), u.isEnabled(), u.getCreatedAt());
    }
}
