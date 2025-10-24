package com.recordmanagement.habitlog.domain.exercise.domain.repository;

/**
 * 운동 기록 관리자 기능 저장소 인터페이스
 * 
 * ISP 적용: 관리자/시스템 기능만 분리
 * - 시스템 관리나 관리자만 필요한 기능
 * - 일반 비즈니스 로직에서는 의존하지 않음
 * - 회원 탈퇴 등 시스템 차원의 작업
 * 
 * 책임:
 * - 사용자 데이터 일괄 삭제
 * - 시스템 유지보수 기능
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (ISP 적용)
 */
public interface ExerciseRecordAdminRepository {

    /**
     * 사용자의 모든 운동 기록 삭제 (관리자/시스템 기능)
     * 
     * 주로 회원 탈퇴 시 사용되는 기능
     * 일반 비즈니스 로직에서는 사용하지 않음
     * 
     * @param userId 삭제할 사용자 ID
     */
    void deleteByUserId(String userId);
}