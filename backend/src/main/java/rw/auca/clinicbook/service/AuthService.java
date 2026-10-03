package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.dto.*;
import rw.auca.clinicbook.entity.*;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.repository.PatientRepository;
import rw.auca.clinicbook.repository.RoleRepository;
import rw.auca.clinicbook.repository.UserRepository;
import rw.auca.clinicbook.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = User.builder()
                .fullName(req.fullName().trim())
                .email(email)
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(req.password()))
                .provider(AuthProvider.LOCAL)
                .build();
        user.getRoles().add(role(RoleName.PATIENT));
        userRepository.save(user);
        patientRepository.save(Patient.builder()
                .user(user).dateOfBirth(req.dateOfBirth()).gender(req.gender()).nationalId(req.nationalId())
                .build());
        auditService.log(user.getId(), "REGISTER", "User", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .filter(u -> u.getPasswordHash() != null && passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is disabled");
        }
        auditService.log(user.getId(), "LOGIN", "User", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse loginWithGoogle(String email, String name, String googleId) {
        if (email == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Google account has no email");
        }
        String normalized = email.toLowerCase();
        User user = userRepository.findByEmail(normalized).orElseGet(() -> {
            User created = User.builder()
                    .fullName(name != null ? name : normalized)
                    .email(normalized)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(googleId)
                    .build();
            created.getRoles().add(role(RoleName.PATIENT));
            userRepository.save(created);
            patientRepository.save(Patient.builder().user(created).build());
            return created;
        });
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is disabled");
        }
        auditService.log(user.getId(), "LOGIN_GOOGLE", "User", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.consume(refreshToken);
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return UserResponse.from(userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User")));
    }

    private AuthResponse issueTokens(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                refreshTokenService.create(user),
                "Bearer",
                jwtService.getAccessExpirationSeconds(),
                UserResponse.from(user));
    }

    private Role role(RoleName name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Role " + name + " is not seeded"));
    }
}
