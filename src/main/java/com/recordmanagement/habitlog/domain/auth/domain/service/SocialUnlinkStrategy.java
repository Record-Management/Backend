package com.recordmanagement.habitlog.domain.auth.domain.service;

import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;

/**
 * 소셜 플랫폼 연결 해제 전략 인터페이스
 * 
 * OCP (Open-Closed Principle) 적용:
 * - 새로운 소셜 플랫폼 추가 시 기존 코드 수정 없이 확장 가능
 * - Strategy 패턴을 통해 각 소셜 플랫폼별 연결 해제 로직을 분리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
public interface SocialUnlinkStrategy {

    /**
     * 소셜 계정 연결 해제
     * 
     * @param socialId 소셜 플랫폼 사용자 ID
     * @throws Exception 연결 해제 실패 시
     */
    void unlinkAccount(String socialId) throws Exception;

    /**
     * 지원하는 소셜 타입 반환
     * 
     * @return 이 전략이 처리하는 소셜 타입
     */
    SocialType getSupportedType();
}