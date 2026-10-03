package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.clinicbook.entity.RefreshToken;
import rw.auca.clinicbook.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
