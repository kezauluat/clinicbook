package rw.auca.clinicbook.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Jwt jwt = jwtService.decode(header.substring(7));
                Long userId = ((Number) jwt.getClaim("uid")).longValue();
                List<String> roles = jwt.getClaimAsStringList("roles");
                List<SimpleGrantedAuthority> authorities = roles == null ? List.of()
                        : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
                var auth = UsernamePasswordAuthenticationToken.authenticated(
                        new AuthUser(userId, jwt.getSubject()), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | ClassCastException | NullPointerException e) {
                SecurityContextHolder.clearContext(); // invalid token -> request stays anonymous -> 401
            }
        }
        chain.doFilter(request, response);
    }
}
