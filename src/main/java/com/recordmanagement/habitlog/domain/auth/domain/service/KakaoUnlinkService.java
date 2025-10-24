package com.recordmanagement.habitlog.domain.auth.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 카카오 계정 연결 해제 서비스
 * 
 * DIP 적용: ExternalApiClient 추상화에 의존
 * - RestTemplate 구체 클래스 의존성 제거
 * - 도메인 서비스가 인프라스트럭처 기술에 독립적
 * - 테스트 용이성 향상
 * 
 * @author 전우선
 * @since 2025.08.01
 * @version 2.0.0 (DIP 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoUnlinkService {

    private final ExternalApiClient externalApiClient;
    
    @Value("${oauth.kakao.admin-key}")
    private String kakaoAdminKey;

    /**
     * 카카오 계정 연결 해제
     * 
     * DIP 적용: 추상화된 ExternalApiClient 사용
     * - 구체적인 HTTP 클라이언트 기술에 의존하지 않음
     * - 카카오 API 호출 로직만 집중
     * 
     * @param socialId 카카오 사용자 ID
     */
    public void unlinkKakaoAccount(String socialId) {
        log.info("카카오 연결 해제 시작: socialId={}", maskSocialId(socialId));
        
        try {
            String url = "https://kapi.kakao.com/v1/user/unlink";
            
            // 헤더 설정
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "KakaoAK " + kakaoAdminKey);
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            
            // 요청 본문 설정 (Form Data)
            KakaoUnlinkRequest requestBody = new KakaoUnlinkRequest(socialId);
            
            // 추상화된 API 클라이언트를 통한 호출
            externalApiClient.post(url, headers, requestBody, String.class);
            
            log.info("카카오 연결 해제 성공: socialId={}", maskSocialId(socialId));
            
        } catch (Exception e) {
            log.error("카카오 연결 해제 실패: socialId={}, error={}", maskSocialId(socialId), e.getMessage());
            throw new RuntimeException("카카오 연결 해제 실패", e);
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

    /**
     * 카카오 연결 해제 요청 객체
     */
    private static class KakaoUnlinkRequest {
        private final String target_id_type = "user_id";
        private final String target_id;

        public KakaoUnlinkRequest(String targetId) {
            this.target_id = targetId;
        }

        public String getTarget_id_type() {
            return target_id_type;
        }

        public String getTarget_id() {
            return target_id;
        }
    }
}
