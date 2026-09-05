package com.skillgap.analyzer.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final CustomUserDetailsService users;
    private final SecurityErrorWriter errors;
    public JwtAuthenticationFilter(JwtService jwt, CustomUserDetailsService users, SecurityErrorWriter errors) {
        this.jwt = jwt; this.users = users; this.errors = errors;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.startsWith("Bearer ") || header.length() <= 7) throw new IllegalArgumentException();
                String token = header.substring(7);
                String email = jwt.extractUsername(token);
                if (email == null || email.isBlank()) throw new IllegalArgumentException();
                var user = users.loadUserByUsername(email);
                if (!jwt.isTokenValid(token, user)) throw new IllegalArgumentException();
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                errors.write(response, 401, "Invalid or expired token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
