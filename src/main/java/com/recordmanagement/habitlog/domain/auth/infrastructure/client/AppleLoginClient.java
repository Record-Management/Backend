package com.recordmanagement.habitlog.domain.auth.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recordmanagement.habitlog.domain.auth.domain.model.SocialUserInfo;
import com.recordmanagement.habitlog.domain.auth.exception.SocialLoginException;
import com.recordmanagement.habitlog.domain.auth.infrastructure.client.dto.AppleTokenResponse;
import com.recordmanagement.habitlog.domain.auth.infrastructure.client.dto.AppleUserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Apple 로그인 클라이언트 구현체 (프로덕션 버전)
 *
 * LSP 적용: SocialLoginClient 계약을 정확히 준수
 * - 일관된 예외 처리로 대체 가능성 보장
 * - Apple Sign in with Apple 서비스와 연동하여 사용자 정보를 조회합니다.
 * 
 * 프로덕션 환경을 위한 개선사항:
 * - 강화된 JWT 검증 (서명, 만료, 발급자)
 * - 상세한 에러 핸들링 및 로깅  
 * - 보안 강화 (토큰 검증 강화)
 * - 캐싱된 클라이언트 시크릿 사용
 * - 모니터링 및 디버깅 지원
 * - Graceful error handling
 *
 * Apple OAuth 플로우:
 * 1. 클라이언트에서 Apple ID Token (JWT) 수신
 * 2. ID Token 서명 및 내용 검증
 * 3. 사용자 정보 추출 (sub, email 등)
 * 4. 필요시 Apple 서버에 추가 검증 요청
 * 
 * @author 전우선
 * @since 2025.07.31
 * @version 3.0.0 (LSP 적용)
 */
@Slf4j
@Component
public class AppleLoginClient implements SocialLoginClient {

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";

    private final RestTemplate restTemplate;
    private final AppleJwtUtils appleJwtUtils;
    private final ObjectMapper objectMapper;
    
    // Apple 공개 키 캐시
    private Map<String, PublicKey> publicKeyCache = new HashMap<>();
    private long lastKeyRefreshTime = 0;
    private static final long KEY_CACHE_DURATION = 24 * 60 * 60 * 1000; // 24시간

    @Value("${social.apple.team-id}")
    private String teamId;

    @Value("${social.apple.key-id}")
    private String keyId;

    @Value("${social.apple.service-id}")
    private String serviceId;

    @Value("${social.apple.bundle-id}")
    private String bundleId;

    @Value("${social.apple.private-key-path}")
    private String privateKeyPath;

    public AppleLoginClient(RestTemplate restTemplate, AppleJwtUtils appleJwtUtils, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.appleJwtUtils = appleJwtUtils;
        this.objectMapper = objectMapper;
        log.info("Apple 로그인 클라이언트 초기화 완료");
    }

    /**
     * Apple 사용자 정보 조회 (identityToken 방식)
     * 
     * Apple identityToken(JWT)을 파싱하여 sub를 추출하고 사용자 정보를 반환합니다.
     * 단순하고 안정적인 방식으로 Apple 로그인을 처리합니다.
     *
     * @param identityToken Apple identityToken (JWT 형태)
     * @return 검증된 사용자 정보 (주로 sub 기반)
     * @throws SocialLoginException 토큰이 유효하지 않거나 검증 실패 시
     */
    @Override
    public SocialUserInfo getUserInfo(String identityToken) throws SocialLoginException {
        try {
            log.info("Apple identityToken 처리 시작");
            
            // 1. 기본 유효성 검사
            validateInputToken(identityToken);
            
            // 2. JWT 구조 검증
            validateJwtStructure(identityToken);
            
            // 3. identityToken 파싱 및 검증 (단순화)
            AppleUserInfo appleUserInfo = parseAndValidateIdToken(identityToken);
            
            // 4. 사용자 정보 로그 (개인정보 제외)
            logUserInfoSafely(appleUserInfo);
            
            // 5. 도메인 객체로 변환 (sub 중심)
            return SocialUserInfo.of(
                appleUserInfo.getSub(),  // 가장 중요한 식별자
                extractUserName(appleUserInfo),
                appleUserInfo.getEmail(),
                null,
                null
            );

        } catch (SocialLoginException e) {
            log.warn("Apple 로그인 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Apple 사용자 정보 조회 중 예상치 못한 오류 발생", e);
            throw SocialLoginException.userInfoFetchFailed();
        }
    }

    /**
     * 입력 토큰 기본 유효성 검사
     */
    private void validateInputToken(String identityToken) {
        if (!StringUtils.hasText(identityToken)) {
            log.error("Apple identityToken이 null이거나 비어있음");
            throw SocialLoginException.invalidAccessToken();
        }
        
        if (identityToken.length() > 8192) { // JWT 최대 길이 제한
            log.error("Apple identityToken이 너무 김: {} bytes", identityToken.length());
            throw SocialLoginException.invalidAccessToken();
        }
    }

    /**
     * JWT 구조 검증 (Header.Payload.Signature)
     */
    private void validateJwtStructure(String identityToken) {
        String[] chunks = identityToken.split("\\.");
        if (chunks.length != 3) {
            log.error("잘못된 JWT 구조: {} 부분으로 구성됨 (3개 필요)", chunks.length);
            throw SocialLoginException.appleIdTokenInvalid();
        }
        
        // Base64 디코딩 테스트
        try {
            Base64.getUrlDecoder().decode(chunks[0]); // Header
            Base64.getUrlDecoder().decode(chunks[1]); // Payload
        } catch (IllegalArgumentException e) {
            log.error("identityToken Base64 디코딩 실패", e);
            throw SocialLoginException.appleIdTokenInvalid();
        }
    }

    /**
     * identityToken 파싱 및 상세 검증 (Apple 공개 키로 서명 검증)
     */
    private AppleUserInfo parseAndValidateIdToken(String identityToken) {
        try {
            // 1. JWT 헤더에서 키 ID 추출
            String[] chunks = identityToken.split("\\.");
            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT structure");
            }
            
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            JsonNode headerNode = objectMapper.readTree(headerJson);
            String keyId = headerNode.get("kid").asText();
            String algorithm = headerNode.get("alg").asText();
            
            log.info("Apple identityToken 헤더 - kid: {}, alg: {}", keyId, algorithm);
            
            // 2. Apple 공개 키 가져오기
            PublicKey publicKey = getApplePublicKey(keyId);
            
            // 3. JWT 서명 검증 및 파싱
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .build()
                    .parseClaimsJws(identityToken)
                    .getBody();

            // 4. 추가 검증
            validateClaims(claims);
            
            return new AppleUserInfo(
                claims.getSubject(),
                claims.get("email", String.class),
                String.valueOf(claims.get("email_verified", Boolean.class)),
                String.valueOf(claims.get("is_private_email", Boolean.class)),
                claims.getIssuer(),
                claims.getAudience(),
                claims.getExpiration().getTime(),
                claims.getIssuedAt().getTime()
            );

        } catch (ExpiredJwtException e) {
            log.error("Apple ID Token 만료: {}", e.getMessage());
            throw SocialLoginException.appleTokenExpired();
        } catch (MalformedJwtException e) {
            log.error("Apple ID Token 형식 오류: {}", e.getMessage());
            throw SocialLoginException.appleIdTokenInvalid();
        } catch (JwtException e) {
            log.error("Apple ID Token JWT 처리 오류: {}", e.getMessage());
            throw SocialLoginException.appleIdTokenInvalid();
        } catch (Exception e) {
            log.error("Apple ID Token 파싱 중 예상치 못한 오류", e);
            throw SocialLoginException.appleIdTokenInvalid();
        }
    }

    /**
     * JWT Claims 상세 검증
     */
    private void validateClaims(Claims claims) {
        // 1. 필수 필드 존재 확인
        if (!StringUtils.hasText(claims.getSubject())) {
            throw new IllegalArgumentException("Apple ID Token에 subject가 없습니다");
        }
        
        // 2. 발급자 검증
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            log.error("잘못된 토큰 발급자: {} (예상: {})", claims.getIssuer(), APPLE_ISSUER);
            throw new IllegalArgumentException("잘못된 토큰 발급자입니다");
        }

        // 3. 만료 시간 확인
        Date now = new Date();
        if (claims.getExpiration().before(now)) {
            log.error("토큰 만료: {} (현재: {})", claims.getExpiration(), now);
            throw new IllegalArgumentException("만료된 토큰입니다");
        }

        // 4. 발급 시간 검증 (미래 시간 방지)
        if (claims.getIssuedAt().after(new Date(now.getTime() + 60000))) { // 1분 여유
            log.error("미래에 발급된 토큰: {} (현재: {})", claims.getIssuedAt(), now);
            throw new IllegalArgumentException("유효하지 않은 발급 시간입니다");
        }

        // 5. Audience 검증 (옵션)
        String audience = claims.getAudience();
        if (StringUtils.hasText(audience) && !audience.equals(bundleId) && !audience.equals(serviceId)) {
            log.warn("예상과 다른 audience: {} (예상: {} 또는 {})", audience, bundleId, serviceId);
        }
    }

    /**
     * Apple 서버에서 토큰 검증 (고급 기능)
     * 
     * 추가 보안이 필요한 경우 사용합니다.
     */
    private AppleTokenResponse validateTokenWithAppleServer(String authorizationCode) {
        try {
            log.info("Apple 서버에서 토큰 검증 시작");
            
            String clientSecret = appleJwtUtils.generateClientSecret(teamId, keyId, serviceId, privateKeyPath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", serviceId);
            params.add("client_secret", clientSecret);
            params.add("code", authorizationCode);
            params.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<AppleTokenResponse> response = restTemplate.postForEntity(
                APPLE_TOKEN_URL,
                request,
                AppleTokenResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Apple 서버 토큰 검증 실패: HTTP {}", response.getStatusCode());
                throw SocialLoginException.loginFailed();
            }

            log.info("Apple 서버 토큰 검증 성공");
            return response.getBody();

        } catch (RestClientException e) {
            log.error("Apple 서버 통신 오류", e);
            throw SocialLoginException.loginFailed();
        } catch (Exception e) {
            log.error("Apple 토큰 검증 중 예상치 못한 오류", e);
            throw SocialLoginException.loginFailed();
        }
    }

    /**
     * 사용자 이름 추출 (개선된 로직)
     */
    private String extractUserName(AppleUserInfo appleUserInfo) {
        String email = appleUserInfo.getEmail();
        
        // 1. 이메일에서 추출 시도
        if (StringUtils.hasText(email) && email.contains("@")) {
            String localPart = email.substring(0, email.indexOf("@"));
            if (StringUtils.hasText(localPart) && localPart.length() >= 2) {
                return localPart;
            }
        }
        
        // 2. Apple 사용자 ID에서 추출
        String sub = appleUserInfo.getSub();
        if (StringUtils.hasText(sub)) {
            // Apple ID 뒷 8자리 사용 (보안상 전체 ID 노출 방지)
            if (sub.length() > 8) {
                return "Apple_" + sub.substring(sub.length() - 8);
            } else {
                return "Apple_" + sub;
            }
        }
        
        // 3. 기본값
        return "Apple 사용자";
    }

    /**
     * 안전한 사용자 정보 로깅 (개인정보 보호)
     */
    private void logUserInfoSafely(AppleUserInfo appleUserInfo) {
        if (log.isInfoEnabled()) {
            String maskedEmail = maskEmail(appleUserInfo.getEmail());
            String maskedSub = maskSub(appleUserInfo.getSub());
            
            log.info("Apple 사용자 정보 추출 완료 - Sub: {}, Email: {}, EmailVerified: {}, PrivateEmail: {}", 
                    maskedSub, maskedEmail, appleUserInfo.isEmailVerified(), appleUserInfo.isPrivateEmail());
        }
    }

    /**
     * 이메일 마스킹 (개인정보 보호)
     */
    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        
        if (local.length() <= 2) {
            return "***@" + domain;
        }
        
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    /**
     * Apple Sub ID 마스킹 (개인정보 보호)
     */
    private String maskSub(String sub) {
        if (!StringUtils.hasText(sub)) {
            return "***";
        }
        
        if (sub.length() <= 8) {
            return "***";
        }
        
        return sub.substring(0, 4) + "***" + sub.substring(sub.length() - 4);
    }

    /**
     * Apple 공개 키 가져오기 (JWKS 엔드포인트 사용)
     */
    private PublicKey getApplePublicKey(String keyId) {
        try {
            // 캐시 확인 및 갱신
            if (needsKeyRefresh() || !publicKeyCache.containsKey(keyId)) {
                refreshApplePublicKeys();
            }
            
            PublicKey publicKey = publicKeyCache.get(keyId);
            if (publicKey == null) {
                log.error("Apple 공개 키를 찾을 수 없음: {}", keyId);
                throw new IllegalArgumentException("Apple 공개 키를 찾을 수 없습니다: " + keyId);
            }
            
            return publicKey;
            
        } catch (Exception e) {
            log.error("Apple 공개 키 조회 실패", e);
            throw new RuntimeException("Apple 공개 키 조회에 실패했습니다", e);
        }
    }
    
    /**
     * Apple JWKS에서 공개 키 목록 갱신
     */
    private synchronized void refreshApplePublicKeys() {
        try {
            log.info("Apple 공개 키 갱신 시작");
            
            ResponseEntity<String> response = restTemplate.getForEntity(APPLE_JWKS_URL, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Apple JWKS 조회 실패: HTTP " + response.getStatusCode());
            }
            
            JsonNode jwksNode = objectMapper.readTree(response.getBody());
            JsonNode keysNode = jwksNode.get("keys");
            
            Map<String, PublicKey> newKeys = new HashMap<>();
            
            for (JsonNode keyNode : keysNode) {
                String kid = keyNode.get("kid").asText();
                String kty = keyNode.get("kty").asText();
                String use = keyNode.has("use") ? keyNode.get("use").asText() : "sig";
                
                if ("RSA".equals(kty) && "sig".equals(use)) {
                    String n = keyNode.get("n").asText();
                    String e = keyNode.get("e").asText();
                    
                    PublicKey publicKey = createRSAPublicKey(n, e);
                    newKeys.put(kid, publicKey);
                    
                    log.debug("Apple 공개 키 로드됨: {}", kid);
                }
            }
            
            publicKeyCache = newKeys;
            lastKeyRefreshTime = System.currentTimeMillis();
            
            log.info("Apple 공개 키 갱신 완료: {} 개 키 로드됨", newKeys.size());
            
        } catch (Exception e) {
            log.error("Apple 공개 키 갱신 실패", e);
            throw new RuntimeException("Apple 공개 키 갱신에 실패했습니다", e);
        }
    }
    
    /**
     * RSA 공개 키 생성
     */
    private PublicKey createRSAPublicKey(String nStr, String eStr) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
        byte[] eBytes = Base64.getUrlDecoder().decode(eStr);
        
        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);
        
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        
        return factory.generatePublic(spec);
    }
    
    /**
     * 키 캐시 갱신이 필요한지 확인
     */
    private boolean needsKeyRefresh() {
        return (System.currentTimeMillis() - lastKeyRefreshTime) > KEY_CACHE_DURATION;
    }

    /**
     * 캐시 상태 조회 (모니터링용)
     */
    public AppleJwtUtils.CacheStats getCacheStats() {
        return appleJwtUtils.getCacheStats();
    }
}
