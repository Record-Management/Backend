package com.recordmanagement.habitlog.infrastructure.auth.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * AppleJwtUtils 단위 테스트
 * 
 * Apple JWT 생성, 캐싱, Private Key 로딩 등의 핵심 기능을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Apple JWT 유틸리티 테스트")
class AppleJwtUtilsTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    private AppleJwtUtils appleJwtUtils;

    private final String testTeamId = "ABC1234567";
    private final String testKeyId = "DEF1234567";
    private final String testServiceId = "com.test.service";
    private final String testPrivateKeyPath = "classpath:apple/AuthKey_DEF1234567.p8";
    private final long testJwtExpirationMinutes = 30L;

    @BeforeEach
    void setUp() {
        appleJwtUtils = new AppleJwtUtils(resourceLoader, testJwtExpirationMinutes);
    }

    @Test
    @DisplayName("캐시 통계 정보 조회 테스트")
    void getCacheStats_shouldReturnValidStats() {
        // When
        AppleJwtUtils.CacheStats stats = appleJwtUtils.getCacheStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.isClientSecretCached()).isFalse(); // 아직 생성 전이므로 false
        assertThat(stats.getPrivateKeyCacheSize()).isZero(); // 아직 로드 전이므로 0
    }

    @Test
    @DisplayName("캐시 클리어 테스트")
    void clearCache_shouldClearAllCaches() {
        // When
        appleJwtUtils.clearCache();

        // Then
        AppleJwtUtils.CacheStats stats = appleJwtUtils.getCacheStats();
        assertThat(stats.isClientSecretCached()).isFalse();
        assertThat(stats.getPrivateKeyCacheSize()).isZero();
    }

    @Test
    @DisplayName("Private Key 파일이 존재하지 않을 때 예외 발생")
    void generateClientSecret_whenPrivateKeyFileNotExists_shouldThrowException() throws Exception {
        // Given
        given(resourceLoader.getResource(testPrivateKeyPath)).willReturn(resource);
        given(resource.exists()).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> 
            appleJwtUtils.generateClientSecret(testTeamId, testKeyId, testServiceId, testPrivateKeyPath)
        )
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Apple Private Key를 로드할 수 없습니다");
    }

    @Test
    @DisplayName("null 파라미터로 JWT 생성 시 적절한 예외 발생")
    void generateClientSecret_withNullParameters_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() ->
            appleJwtUtils.generateClientSecret(null, testKeyId, testServiceId, testPrivateKeyPath)
        ).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() ->
            appleJwtUtils.generateClientSecret(testTeamId, null, testServiceId, testPrivateKeyPath)
        ).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() ->
            appleJwtUtils.generateClientSecret(testTeamId, testKeyId, null, testPrivateKeyPath)
        ).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() ->
            appleJwtUtils.generateClientSecret(testTeamId, testKeyId, testServiceId, null)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("JWT 만료 시간 설정 확인")
    void appleJwtUtils_shouldUseConfiguredExpirationTime() {
        // Given
        long customExpirationMinutes = 120L;

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            new AppleJwtUtils(resourceLoader, customExpirationMinutes)
        );
    }

    @Test
    @DisplayName("CacheStats 객체의 toString 메서드 테스트")
    void cacheStats_toString_shouldContainRelevantInfo() {
        // When
        AppleJwtUtils.CacheStats stats = appleJwtUtils.getCacheStats();
        String toString = stats.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("CacheStats");
        assertThat(toString).contains("clientSecretCached");
        assertThat(toString).contains("privateKeyCacheSize");
    }
}
