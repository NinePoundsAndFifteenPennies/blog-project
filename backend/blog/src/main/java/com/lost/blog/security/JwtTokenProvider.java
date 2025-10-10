package com.lost.blog.security; // 确保包名正确

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component // 声明为Spring组件，方便其他地方注入
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey jwtSecretKey;
    private final long jwtExpirationInMs;
    private final long jwtRememberMeExpirationInMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String jwtSecret,
                            @Value("${app.jwt.expiration-ms}") long jwtExpirationInMs,
                            @Value("${app.jwt.remember-me-expiration-ms:2592000000}") long jwtRememberMeExpirationInMs) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.jwtRememberMeExpirationInMs = jwtRememberMeExpirationInMs;
    }

    // 生成Token (不记住我 - 短期token)
    public String generateToken(Authentication authentication) {
        return generateToken(authentication, false);
    }

    // 生成Token (支持记住我)
    public String generateToken(Authentication authentication, boolean rememberMe) {
        User userPrincipal = (User) authentication.getPrincipal();

        Date now = new Date();
        long expirationTime = rememberMe ? jwtRememberMeExpirationInMs : jwtExpirationInMs;
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // 从Token中获取用户名
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 验证Token
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}
