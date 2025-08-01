package com.recordmanagement.habitlog.domain.auth.service;

import com.recordmanagement.habitlog.domain.user.model.SocialType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 소셜 플랫폼 연결 해제 도메인 서비스
 * 
 * 카카오, 애플 등 소셜 플랫폼과의 연결을 해제하는 기능을 제공합니다.
 * 회원탈퇴 시 사용자의 소셜 계정에서 앱 권한을 취소합니다.
 * 
 * @author 전우선
 * @since 2025.08.01
 * @version 1.0.0
 */
@Slf4j
@Service
public class SocialUnlinkService {

    private final KakaoUnlinkService kakaoUnlinkService;
    private final AppleUnlinkService appleUnlinkService;

    public SocialUnlinkService(KakaoUnlinkService kakaoUnlinkService, 
                              AppleUnlinkService appleUnlinkService) {
        this.kakaoUnlinkService = kakaoUnlinkService;
        this.appleUnlinkService = appleUnlinkService;
    }

    /**
     * 소셜 플랫폼 연결 해제
     * 
     * @param socialType 소셜 플랫폼 타입
     * @param socialId 소셜 플랫폼 사용자 ID
     */
    public void unlinkSocialConnection(SocialType socialType, String socialId) {
        try {
            switch (socialType) {
                case KAKAO -> {
                    log.info("카카오 연결 해제 시작: socialId={}", socialId);
                    kakaoUnlinkService.unlinkKakaoAccount(socialId);
                    log.info("카카오 연결 해제 완료: socialId={}", socialId);
                }
                case APPLE -> {
                    log.info("애플 연결 해제 시작: socialId={}", socialId);
                    appleUnlinkService.unlinkAppleAccount(socialId);
                    log.info("애플 연결 해제 완료: socialId={}", socialId);
                }
            }
        } catch (Exception e) {
            log.warn("소셜 연결 해제 실패 (계속 진행): socialType={}, socialId={}, error={}", 
                    socialType, socialId, e.getMessage());
            // 연결 해제 실패해도 회원탈퇴는 계속 진행
        }
    }
}
