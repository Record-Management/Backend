package com.recordmanagement.habitlog.domain.exercise.infrastructure.repository;

import com.recordmanagement.habitlog.domain.exercise.infrastructure.entity.ExerciseRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaExerciseRecordRepository extends JpaRepository<ExerciseRecordEntity, String> {

    Optional<ExerciseRecordEntity> findByExerciseRecordIdAndUserId(String exerciseRecordId, String userId);

    List<ExerciseRecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);

    List<ExerciseRecordEntity> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    void deleteByExerciseRecordIdAndUserId(String exerciseRecordId, String userId);

    boolean existsByExerciseRecordIdAndUserId(String exerciseRecordId, String userId);

    int countByUserIdAndRecordDate(String userId, LocalDate recordDate);

    /**
     * 특정 기간 내 운동 기록이 있는 날짜 수를 조회 (성능 최적화)
     * - DISTINCT를 사용하여 중복 날짜 제거
     * - 목표 달성률 계산 시 N번 쿼리 대신 1번 쿼리로 처리
     */
    @Query("SELECT COUNT(DISTINCT e.recordDate) FROM ExerciseRecordEntity e " +
           "WHERE e.userId = :userId " +
           "AND e.recordDate BETWEEN :startDate AND :endDate")
    int countDistinctRecordDatesByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 사용자 ID로 모든 운동 기록 삭제 (회원 탈퇴 시)
    void deleteByUserId(String userId);
}