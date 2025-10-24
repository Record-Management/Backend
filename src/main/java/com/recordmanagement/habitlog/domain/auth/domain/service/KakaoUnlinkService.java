package com.recordmanagement.habitlog.domain.auth.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 카카오 계정 연결 해제 서비스
 * 
 * 카카오 API를 호출하여 사용자의 카카오 계정에서 앱 연결을 해제합니다.
 * 
 * @author 전우선
 * @since 2025.08.01
 * @version 1.0.0
 */
@Slf4j
@Service
public class KakaoUnlinkService {

    private final RestTemplate restTemplate;
    private final String kakaoAdminKey;

    public KakaoUnlinkService(RestTemplate restTemplate,
                             @Value("${oauth.kakao.admin-key}") String kakaoAdminKey) {
        this.restTemplate = restTemplate;
        this.kakaoAdminKey = kakaoAdminKey;
    }

    /**
     * 카카오 계정 연결 해제
     * 
     * @param socialId 카카오 사용자 ID
     */
    public void unlinkKakaoAccount(String socialId) {
        try {
            // 카카오 API 연결해제 엔드포인트
            String url = "https://kapi.kakao.com/v1/user/unlink";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Admin Key 사용 (서버에서 직접 연결해제)
            headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("target_id_type", "user_id");
            params.add("target_id", socialId);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            log.info("카카오 연결 해제 성공: socialId={}", socialId);
            
        } catch (Exception e) {
            log.error("카카오 연결 해제 실패: socialId={}, error={}", socialId, e.getMessage());
            throw new RuntimeException("카카오 연결 해제 실패", e);
        }
    }
}
