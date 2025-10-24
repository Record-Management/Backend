package com.recordmanagement.habitlog.domain.auth.domain.service;

import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 소셜 플랫폼 연결 해제 도메인 서비스
 * 
 * OCP 적용: Strategy 패턴을 통한 확장 가능한 설계
 * - 새로운 소셜 플랫폼 추가 시 기존 코드 수정 없이 확장 가능
 * - Factory 패턴을 통한 전략 관리로 switch 문 제거
 * 
 * @author 전우선
 * @since 2025.08.01
 * @version 2.0.0 (OCP 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialUnlinkService {

    private final SocialUnlinkStrategyFactory strategyFactory;

    /**
     * 소셜 플랫폼 연결 해제
     * 
     * OCP 적용: Strategy 패턴으로 switch 문 제거
     * - 새로운 소셜 플랫폼 추가 시 기존 코드 수정 불필요
     * - 각 플랫폼별 전략이 독립적으로 동작
     * 
     * @param socialType 소셜 플랫폼 타입
     * @param socialId 소셜 플랫폼 사용자 ID
     */
    public void unlinkSocialConnection(SocialType socialType, String socialId) {
        try {
            log.info("소셜 연결 해제 시작: socialType={}, socialId={}", socialType, maskSocialId(socialId));
            
            SocialUnlinkStrategy strategy = strategyFactory.getStrategy(socialType);
            strategy.unlinkAccount(socialId);
            
            log.info("소셜 연결 해제 완료: socialType={}, socialId={}", socialType, maskSocialId(socialId));
        } catch (Exception e) {
            log.warn("소셜 연결 해제 실패 (계속 진행): socialType={}, socialId={}, error={}", 
                    socialType, maskSocialId(socialId), e.getMessage());
            // 연결 해제 실패해도 회원탈퇴는 계속 진행
        }
    }

    /**
     * 소셜 ID 마스킹 (로깅용)
     */
    private String maskSocialId(String socialId) {
        if (socialId == null || socialId.length() <= 6) {
            return "****";
        }
        return socialId.substring(0, 3) + "****" + socialId.substring(socialId.length() - 3);
    }
}
