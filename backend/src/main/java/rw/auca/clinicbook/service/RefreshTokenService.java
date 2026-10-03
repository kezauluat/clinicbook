package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import rw.auca.clinicbook.entity.RefreshToken;
import rw.auca.clinicbook.entity.User;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.repository.RefreshTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    public String create(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID() + "." + UUID.randomUUID())
                .expiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS))
                .build();
        return refreshTokenRepository.save(token).getToken();
    }

    /** One-time use: the old token is deleted and the caller issues a new one (rotation). */
    public User consume(String token) {
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        User user = stored.getUser();
        refreshTokenRepository.delete(stored);
        return user;
    }

    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}
