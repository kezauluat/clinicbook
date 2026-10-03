package rw.auca.clinicbook.security;

/** The logged-in user, taken from the JWT on every request (no DB lookup). */
public record AuthUser(Long id, String email) {}
