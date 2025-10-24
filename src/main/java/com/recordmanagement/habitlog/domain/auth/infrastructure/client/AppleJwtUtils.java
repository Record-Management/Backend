package com.recordmanagement.habitlog.domain.auth.infrastructure.client;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Apple Sign In JWT 생성 및 캐싱 유틸리티
 * 
 * 프로덕션 환경을 위한 기능:
 * - 클라이언트 시크릿 JWT 캐싱 (성능 최적화)
 * - Private Key 캐싱 (파일 IO 최소화)
 * - 자동 갱신 (만료 전 미리 갱신)
 * - 스레드 안전성 보장
 * - 설정 가능한 만료 시간
 * - 에러 핸들링 및 로깅
 *
 * @author 전우선
 * @since 2025.07.31
 * @version 2.0.0
 */
@Slf4j
@Component
public class AppleJwtUtils {

    /** Apple OAuth 엔드포인트 */
    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";
    
    /** JWT 갱신 버퍼 시간 (만료 10분 전에 미리 갱신) */
    private static final Duration REFRESH_BUFFER = Duration.ofMinutes(10);

    private final ResourceLoader resourceLoader;
    private final long jwtExpirationMinutes;

    // 캐싱을 위한 필드들 (스레드 안전)
    private final ConcurrentHashMap<String, CachedPrivateKey> privateKeyCache = new ConcurrentHashMap<>();
    private volatile CachedClientSecret cachedClientSecret;
    private final ReentrantLock secretGenerationLock = new ReentrantLock();

    public AppleJwtUtils(
            ResourceLoader resourceLoader,
            @Value("${social.apple.jwt-expiration-minutes:60}") long jwtExpirationMinutes) {
        this.resourceLoader = resourceLoader;
        this.jwtExpirationMinutes = jwtExpirationMinutes;
        log.info("Apple JWT Utils 초기화 완료 - JWT 만료 시간: {}분", jwtExpirationMinutes);
    }

    /**
     * Apple API 호출을 위한 클라이언트 시크릿 JWT 생성 (캐싱 적용)
     */
    public String generateClientSecret(String teamId, String keyId, String serviceId, String privateKeyPath) {
        try {
            // 캐시된 시크릿이 유효한지 확인
            CachedClientSecret cached = this.cachedClientSecret;
            if (cached != null && cached.isValid()) {
                log.debug("캐시된 Apple 클라이언트 시크릿 재사용");
                return cached.getSecret();
            }

            // 스레드 안전하게 새 시크릿 생성
            secretGenerationLock.lock();
            try {
                // 더블 체크 (다른 스레드가 이미 생성했을 수 있음)
                cached = this.cachedClientSecret;
                if (cached != null && cached.isValid()) {
                    return cached.getSecret();
                }

                log.info("새로운 Apple 클라이언트 시크릿 생성 시작 - TeamID: {}, KeyID: {}", teamId, keyId);
                String newSecret = createNewClientSecret(teamId, keyId, serviceId, privateKeyPath);
                
                // 캐시 저장
                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(jwtExpirationMinutes);
                this.cachedClientSecret = new CachedClientSecret(newSecret, expiresAt);
                
                log.info("Apple 클라이언트 시크릿 생성 완료 - 만료 시간: {}", expiresAt);
                return newSecret;

            } finally {
                secretGenerationLock.unlock();
            }

        } catch (Exception e) {
            log.error("Apple 클라이언트 시크릿 생성 실패 - TeamID: {}, KeyID: {}", teamId, keyId, e);
            throw new RuntimeException("Apple 클라이언트 시크릿 생성에 실패했습니다", e);
        }
    }

    /**
     * 새로운 클라이언트 시크릿 JWT 생성
     */
    private String createNewClientSecret(String teamId, String keyId, String serviceId, String privateKeyPath) {
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        
        Date now = new Date();
        Date expiration = Date.from(
            LocalDateTime.now().plusMinutes(jwtExpirationMinutes)
                .atZone(ZoneId.systemDefault()).toInstant()
        );

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(teamId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setAudience(APPLE_AUDIENCE)
                .setSubject(serviceId)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    /**
     * Private Key 조회 (캐싱 적용)
     */
    private PrivateKey getPrivateKey(String privateKeyPath) {
        return privateKeyCache.computeIfAbsent(privateKeyPath, this::loadPrivateKeyFromFile)
                .getPrivateKey();
    }

    /**
     * Apple Private Key 파일 로드 (실제 파일 IO)
     */
    private CachedPrivateKey loadPrivateKeyFromFile(String privateKeyPath) {
        try {
            log.info("Apple Private Key 파일 로드 시작: {}", privateKeyPath);
            Resource resource = resourceLoader.getResource(privateKeyPath);
            
            if (!resource.exists()) {
                throw new IllegalArgumentException("Apple Private Key 파일을 찾을 수 없습니다: " + privateKeyPath);
            }
            
            try (PEMParser pemParser = new PEMParser(new InputStreamReader(resource.getInputStream()))) {
                Object pemObject = pemParser.readObject();
                
                if (!(pemObject instanceof PrivateKeyInfo)) {
                    throw new IllegalArgumentException("유효하지 않은 Private Key 형식입니다: " + privateKeyPath);
                }
                
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                PrivateKey privateKey = converter.getPrivateKey((PrivateKeyInfo) pemObject);
                
                log.info("Apple Private Key 로드 완료: {}", privateKeyPath);
                return new CachedPrivateKey(privateKey, LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Apple Private Key 로드 실패: {}", privateKeyPath, e);
            throw new RuntimeException("Apple Private Key를 로드할 수 없습니다: " + privateKeyPath, e);
        }
    }

    /**
     * 캐시 통계 정보 (모니터링용)
     */
    public CacheStats getCacheStats() {
        CachedClientSecret secret = this.cachedClientSecret;
        boolean secretCached = secret != null && secret.isValid();
        int privateKeyCacheSize = privateKeyCache.size();
        
        return new CacheStats(secretCached, privateKeyCacheSize);
    }

    /**
     * 캐시 수동 클리어 (필요시 사용)
     */
    public void clearCache() {
        secretGenerationLock.lock();
        try {
            this.cachedClientSecret = null;
            this.privateKeyCache.clear();
            log.info("Apple JWT 캐시 클리어 완료");
        } finally {
            secretGenerationLock.unlock();
        }
    }

    // ============ 내부 클래스들 (Lombok 적용) ============

    /**
     * 캐시된 클라이언트 시크릿
     */
    @Getter
    @RequiredArgsConstructor
    private static class CachedClientSecret {
        private final String secret;
        private final LocalDateTime expiresAt;

        public boolean isValid() {
            return LocalDateTime.now().isBefore(expiresAt.minus(REFRESH_BUFFER));
        }
    }

    /**
     * 캐시된 Private Key
     */
    @Getter
    @RequiredArgsConstructor
    private static class CachedPrivateKey {
        private final PrivateKey privateKey;
        private final LocalDateTime loadedAt;
    }

    /**
     * 캐시 통계 정보
     */
    @Getter
    @RequiredArgsConstructor
    @ToString
    public static class CacheStats {
        private final boolean clientSecretCached;
        private final int privateKeyCacheSize;
    }

}
