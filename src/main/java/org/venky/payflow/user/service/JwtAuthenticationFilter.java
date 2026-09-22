package org.venky.payflow.user.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.venky.payflow.user.security.AuthenticatedUser;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authToken = authHeader.substring(7);

            String email = jwtService.extractEmail(authToken);
            String role = jwtService.extractRole(authToken);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UUID userId = jwtService.extractUserId(authToken);

            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(
                            userId,
                            email,
                            authorities
                    );
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authenticatedUser, authToken, authorities);

            SecurityContextHolder.getContext().setAuthentication(token);

        }
        catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
