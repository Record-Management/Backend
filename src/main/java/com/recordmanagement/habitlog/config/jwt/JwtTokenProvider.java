package com.recordmanagement.habitlog.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증 제공자
 *
 * JWT 액세스 토큰 및 리프레시 토큰 생성과 검증을 담당하는 컴포넌트입니다.
 * HS256 알고리즘 기반의 HMAC SHA-256 서명을 사용하며,
 * 토큰의 서명 검증, 만료 시간 체크, 사용자 ID 추출 기능을 제공합니다.
 *
 * 설정 파일(application.yml)에서 비밀키와 만료 시간을 주입받아 초기화됩니다.
 *
 * 주요 기능:
 * - 액세스 토큰 및 리프레시 토큰 생성
 * - 토큰에서 사용자 ID 추출 (Long/String 타입 지원)
 * - 토큰 유효성 검증 및 만료 시간 조회
 *
 * 보안:
 * - Base64 인코딩된 비밀키로 서명 생성
 * - 변조 및 위조 방지
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** JWT 서명용 비밀키 (HMAC SHA-256) */
    private final SecretKey key;

    /** 액세스 토큰 만료 시간 (밀리초, 기본: 1시간) */
    private final long accessTokenExpireTime;

    /** 리프레시 토큰 만료 시간 (밀리초, 기본: 30일) */
    private final long refreshTokenExpireTime;

    /**
     * JWT 토큰 프로바이더 생성자
     *
     * @param secretKey Base64 인코딩된 JWT 서명 비밀키 (application.yml)
     * @param accessTokenExpireTime 액세스 토큰 만료 시간 (밀리초)
     * @param refreshTokenExpireTime 리프레시 토큰 만료 시간 (밀리초)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expire-time}") long accessTokenExpireTime,
            @Value("${jwt.refresh-token-expire-time}") long refreshTokenExpireTime) {

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }

    /**
     * 액세스 토큰 생성 (Long 타입 사용자 ID)
     *
     * @param userId 사용자 고유 ID (Long 타입)
     * @return JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(Long userId) {
        return generateAccessToken(String.valueOf(userId));
    }

    /**
     * 액세스 토큰 생성 (String 타입 사용자 ID)
     *
     * @param userId 사용자 고유 ID (String 타입)
     * @return JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(String userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessTokenExpireTime);
        String jti = UUID.randomUUID().toString(); // 토큰 고유 ID 생성

        return Jwts.builder()
                .setId(jti) // JWT ID 설정 (블랙리스트용)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰 생성 (Long 타입 사용자 ID)
     *
     * @param userId 사용자 고유 ID (Long 타입)
     * @return JWT 리프레시 토큰 문자열
     */
    public String generateRefreshToken(Long userId) {
        return generateRefreshToken(String.valueOf(userId));
    }

    /**
     * 리프레시 토큰 생성 (String 타입 사용자 ID)
     *
     * @param userId 사용자 고유 ID (String 타입)
     * @return JWT 리프레시 토큰 문자열
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshTokenExpireTime);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출 (Long 타입 반환)
     *
     * @param token JWT 토큰 문자열
     * @return 사용자 ID (Long 타입)
     * @throws CustomException 사용자 ID가 숫자가 아닌 경우 발생
     */
    public Long getUserIdFromToken(String token) {
        String userId = getUserIdAsStringFromToken(token);
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.JWT_USER_ID_INVALID_FORMAT);
        }
    }

    /**
     * 토큰에서 사용자 ID 추출 (String 타입 반환)
     *
     * @param token JWT 토큰 문자열
     * @return 사용자 ID (String 타입)
     * @throws JwtException 토큰이 유효하지 않은 경우 발생
     */
    public String getUserIdAsStringFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 토큰에서 JWT ID(jti) 추출
     *
     * @param token JWT 토큰 문자열
     * @return JWT ID (jti claim)
     * @throws JwtException 토큰이 유효하지 않은 경우 발생
     */
    public String getTokenIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getId();
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token JWT 토큰 문자열
     * @return true: 유효, false: 무효
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 만료까지 남은 시간 계산
     *
     * @param token JWT 토큰 문자열
     * @return 만료까지 남은 시간(밀리초), 음수면 이미 만료됨
     */
    public long getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}
