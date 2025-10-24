package com.recordmanagement.habitlog.domain.exercise.domain.repository;

/**
 * 운동 기록 통합 저장소 인터페이스
 * 
 * ISP 적용: 여러 책임별 인터페이스를 조합한 복합 인터페이스
 * - 일반적인 비즈니스 로직에서 사용하는 기본 인터페이스
 * - 각 기능별로 분리된 인터페이스들을 extends로 조합
 * - 클라이언트는 필요에 따라 개별 인터페이스나 복합 인터페이스 선택 가능
 * 
 * 포함된 기능:
 * - 기본 CRUD (ExerciseRecordBasicRepository)
 * - 조회 기능 (ExerciseRecordQueryRepository)
 * - 보안 검증 (ExerciseRecordSecurityRepository)
 * - 관리자 기능은 별도 인터페이스로 분리 (ExerciseRecordAdminRepository)
 * 
 * 사용 가이드:
 * - 일반 비즈니스 로직: 이 인터페이스 사용
 * - 읽기 전용 로직: ExerciseRecordQueryRepository 사용
 * - 관리자 기능: ExerciseRecordAdminRepository 추가 사용
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 2.0.0 (ISP 적용)
 */
public interface ExerciseRecordRepository extends 
    ExerciseRecordBasicRepository, 
    ExerciseRecordQueryRepository, 
    ExerciseRecordSecurityRepository {
    
    // 이 인터페이스는 기본적인 비즈니스 로직에서 필요한 기능들만 조합
    // 관리자 기능(ExerciseRecordAdminRepository)은 별도로 필요시에만 의존
}