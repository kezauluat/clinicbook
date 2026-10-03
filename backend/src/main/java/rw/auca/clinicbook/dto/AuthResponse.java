package rw.auca.clinicbook.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType,
                           long expiresInSeconds, UserResponse user) {}
