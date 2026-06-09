package com.recordmanagement.habitlog.domain.exercise.domain.repository;

import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
import java.util.List;

/**
 * 운동 기록 조회 전용 저장소 인터페이스
 * 
 * ISP 적용: 조회 기능만 분리
 * - 읽기 전용 클라이언트를 위한 인터페이스
 * - 복잡한 조회 로직과 통계 기능 제공
 * - 수정/삭제 권한이 없는 안전한 인터페이스
 * 
 * 책임:
 * - 날짜별 조회
 * - 범위별 조회  
 * - 통계 정보 제공
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (ISP 적용)
 */
public interface ExerciseRecordQueryRepository {

    /**
     * 사용자의 특정 날짜 운동 기록 조회
     * 
     * @param userId 사용자 ID
     * @param recordDate 조회할 날짜
     * @return 해당 날짜의 운동 기록 목록
     */
    List<ExerciseRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);

    /**
     * 사용자의 날짜 범위별 운동 기록 조회
     * 
     * @param userId 사용자 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간의 운동 기록 목록
     */
    List<ExerciseRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate);

    /**
     * 사용자의 특정 날짜 운동 기록 개수 조회
     *
     * @param userId 사용자 ID
     * @param recordDate 조회할 날짜
     * @return 해당 날짜의 운동 기록 개수
     */
    int countByUserIdAndRecordDate(UserId userId, LocalDate recordDate);

    /**
     * 특정 기간 내 운동 기록이 있는 날짜 수를 조회 (성능 최적화)
     * - 목표 달성률 계산 시 사용
     * - N번 쿼리 대신 1번 쿼리로 처리
     *
     * @param userId 사용자 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 기록이 있는 날짜 수
     */
    int countDistinctRecordDatesByUserIdAndDateRange(UserId userId, LocalDate startDate, LocalDate endDate);
}