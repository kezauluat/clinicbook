package rw.auca.clinicbook.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import rw.auca.clinicbook.entity.Role;
import rw.auca.clinicbook.entity.RoleName;
import rw.auca.clinicbook.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService("test-secret-key-that-is-long-enough-for-hs256-signing", 60_000);

    private User patient() {
        User user = User.builder().id(7L).email("patient@test.rw").fullName("Test Patient").build();
        user.getRoles().add(new Role(1L, RoleName.PATIENT));
        return user;
    }

    @Test
    void tokenContainsEmailUserIdAndRoles() {
        Jwt jwt = jwtService.decode(jwtService.generateAccessToken(patient()));

        assertThat(jwt.getSubject()).isEqualTo("patient@test.rw");
        assertThat(((Number) jwt.getClaim("uid")).longValue()).isEqualTo(7L);
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("PATIENT");
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtService attacker = new JwtService("a-completely-different-secret-key-for-hs256-signing!!", 60_000);
        String forged = attacker.generateAccessToken(patient());

        assertThatThrownBy(() -> jwtService.decode(forged)).isInstanceOf(JwtException.class);
    }
}
