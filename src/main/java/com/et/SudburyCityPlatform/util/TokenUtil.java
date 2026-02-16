package com.et.SudburyCityPlatform.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class TokenUtil {

  private static String secret;

  @Value("${jwt.secret}")
  public void setSecret(String secret) {
    TokenUtil.secret = secret;
  }

  private static Key getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public static String generateJwtToken(String username, String role) {
    return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000))
            .signWith(getSigningKey())
            .compact();
  }

  public static Claims validateSignedToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .setAllowedClockSkewSeconds(60)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }
}
