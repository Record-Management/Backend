package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.infrastructure.record.entity.RecordEntity;
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
    
    List<RecordEntity> findByUserIdAndType(String userId, RecordType type);
    
    @Query("SELECT r FROM RecordEntity r WHERE r.userId = :userId ORDER BY r.recordDate DESC, r.createdAt DESC")
    List<RecordEntity> findByUserIdOrderByRecordDateDescCreatedAtDesc(@Param("userId") String userId);
    
    // 페이지네이션 지원 메서드 추가
    Page<RecordEntity> findByUserIdOrderByRecordDateDescCreatedAtDesc(@Param("userId") String userId, Pageable pageable);
}