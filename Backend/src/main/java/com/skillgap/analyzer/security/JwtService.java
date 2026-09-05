package com.skillgap.analyzer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        if (expirationMillis <= 0) throw new IllegalArgumentException("jwt.expiration must be positive");
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserDetails user) {
        Instant now = Instant.now();
        String role = user.getAuthorities().iterator().next().getAuthority();
        return Jwts.builder().subject(user.getUsername()).claim("role", role)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key).compact();
    }
    public String extractUsername(String token) { return claims(token).getSubject(); }
    public String extractRole(String token) { return claims(token).get("role", String.class); }
    public Date extractExpiration(String token) { return claims(token).getExpiration(); }

    public boolean isTokenValid(String token, UserDetails user) {
        try {
            Claims claims = claims(token);
            String role = claims.get("role", String.class);
            return user.getUsername().equals(claims.getSubject()) && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date()) && user.isEnabled()
                    && user.isAccountNonLocked() && user.isAccountNonExpired() && user.isCredentialsNonExpired()
                    && user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
