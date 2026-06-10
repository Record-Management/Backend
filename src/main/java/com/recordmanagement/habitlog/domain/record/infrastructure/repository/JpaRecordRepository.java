package com.recordmanagement.habitlog.domain.auth.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.record.infrastructure.entity.RecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JpaRecordRepository extends JpaRepository<RecordEntity, String> {
    
    List<RecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    List<RecordEntity> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT r FROM RecordEntity r WHERE r.userId = :userId ORDER BY r.recordDate DESC, r.createdAt DESC")
    List<RecordEntity> findByUserIdOrderByRecordDateDescCreatedAtDesc(@Param("userId") String userId);
    
    // 페이지네이션 지원 메서드 추가
    Page<RecordEntity> findByUserIdOrderByRecordDateDescCreatedAtDesc(@Param("userId") String userId, Pageable pageable);
    
    // 캘린더 타입별 필터링 지원 메서드
    List<RecordEntity> findByUserIdAndRecordDateBetweenAndTypeIn(String userId, LocalDate startDate, LocalDate endDate, List<RecordType> types);
    
    // 특정 날짜의 특정 타입 기록 개수 조회
    int countByUserIdAndRecordDateAndType(String userId, LocalDate recordDate, RecordType type);

    /**
     * 특정 기간 내 기록이 있는 날짜 수를 조회 (성능 최적화)
     * - DISTINCT를 사용하여 중복 날짜 제거
     * - 목표 달성률 계산 시 N번 쿼리 대신 1번 쿼리로 처리
     */
    @Query("SELECT COUNT(DISTINCT r.recordDate) FROM RecordEntity r " +
           "WHERE r.userId = :userId " +
           "AND r.recordDate BETWEEN :startDate AND :endDate " +
           "AND r.type = :type")
    int countDistinctRecordDatesByUserIdAndDateRangeAndType(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("type") RecordType type
    );

    // 사용자 ID로 모든 기록 삭제 (회원 탈퇴 시)
    void deleteByUserId(String userId);

    // 특정 날짜의 특정 타입 기록 존재 여부 확인
    boolean existsByUserIdAndRecordDateAndType(String userId, LocalDate recordDate, RecordType type);
}