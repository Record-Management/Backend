package com.recordmanagement.habitlog.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 캐시 설정
 * 
 * 성능 최적화를 위한 캐싱 전략:
 * - calendar: 캘린더 조회 결과 (TTL: 10분)
 * - user: 사용자 정보 (TTL: 30분)
 * 
 * TODO: 운영 환경에서는 Redis로 교체 권장
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        // 개발 환경용 인메모리 캐시 (운영에서는 Redis 사용 권장)
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of("calendar", "user"));
        return cacheManager;
    }
}