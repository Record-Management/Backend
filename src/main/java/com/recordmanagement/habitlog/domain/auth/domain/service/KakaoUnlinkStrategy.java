package com.recordmanagement.habitlog.domain.auth.domain.service;

import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 카카오 연결 해제 전략 구현체
 * 
 * OCP 적용: SocialUnlinkStrategy 인터페이스 구현
 * - 카카오 특화 연결 해제 로직 캡슐화
 * - 다른 소셜 플랫폼과 독립적으로 관리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoUnlinkStrategy implements SocialUnlinkStrategy {

    private final KakaoUnlinkService kakaoUnlinkService;

    @Override
    public void unlinkAccount(String socialId) throws Exception {
        log.info("카카오 연결 해제 시작: socialId={}", maskSocialId(socialId));
        
        try {
            kakaoUnlinkService.unlinkKakaoAccount(socialId);
            log.info("카카오 연결 해제 완료: socialId={}", maskSocialId(socialId));
        } catch (Exception e) {
            log.error("카카오 연결 해제 실패: socialId={}, error={}", 
                    maskSocialId(socialId), e.getMessage());
            throw e;
        }
    }

    @Override
    public SocialType getSupportedType() {
        return SocialType.KAKAO;
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