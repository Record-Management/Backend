package com.recordmanagement.habitlog.global.config.scheduler;

import com.recordmanagement.habitlog.domain.auth.domain.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RefreshTokenCleanupScheduler
 *
 * 만료된 리프레시 토큰을 주기적으로 정리하는 스케줄러입니다.
 *
 * 주요 기능:
 * - 매일 오전 3시에 만료된 리프레시 토큰을 삭제하는 작업 수행
 * - 삭제 작업의 시작과 완료, 오류를 로그로 기록
 *
 * 스케줄링 방식:
 * - Spring의 @Scheduled(cron) 어노테이션 사용
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    /**
     * 매일 오전 3시에 실행되어 만료된 리프레시 토큰을 정리합니다.
     *
     * @Scheduled(cron = "0 0 3 * * *") 매일 3시 0분 0초 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredRefreshTokens() {
        try {
            log.info("만료된 리프레시 토큰 정리 작업 시작");
            refreshTokenService.cleanupExpiredTokens();
            log.info("만료된 리프레시 토큰 정리 작업 완료");
        } catch (Exception e) {
            log.error("만료된 리프레시 토큰 정리 작업 중 오류 발생", e);
        }
    }
}