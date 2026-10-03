package rw.auca.clinicbook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.clinicbook.dto.UserResponse;
import rw.auca.clinicbook.security.AuthUser;
import rw.auca.clinicbook.service.AuthService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return authService.me(user.id());
    }
}
