package com.recordmanagement.habitlog.domain.auth.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 애플 계정 연결 해제 서비스
 * 
 * 애플의 경우 서버에서 직접 연결해제하는 API가 제한적이므로,
 * 현재는 로그 기록만 하고 실제 연결해제는 사용자가 애플 설정에서 직접 해야 합니다.
 * 
 * 향후 애플 Server-to-Server 연결해제 API가 필요한 경우 추가 구현 예정
 * 
 * @author 전우선
 * @since 2025.08.01
 * @version 1.0.0
 */
@Slf4j
@Service
public class AppleUnlinkService {

    /**
     * 애플 계정 연결 해제 처리
     * 
     * 현재는 로그 기록만 하며, 실제 연결해제는 사용자가 직접 해야 합니다.
     * 애플 설정 > Apple ID > 미디어 및 구입 > Apple ID 보기 > 구독 > 앱에서 연결 해제
     * 
     * @param socialId 애플 사용자 ID
     */
    public void unlinkAppleAccount(String socialId) {
        log.info("애플 계정 연결해제 요청 기록: socialId={}", socialId);
        log.info("애플은 사용자가 직접 설정에서 연결해제해야 합니다.");
        
        // 현재는 서버에서 직접 연결해제할 수 있는 공식 API가 제한적임
        
        // 연결해제 실패로 처리하지 않고 정상 처리
        // 사용자 계정 삭제는 정상적으로 진행됨
    }
}
