package com.recordmanagement.habitlog.infrastructure.record.entity;

import com.recordmanagement.habitlog.domain.record.model.HabitType;
import com.recordmanagement.habitlog.domain.record.model.HabitRecord;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 습관 기록 JPA 엔티티
 */
@Entity
@Table(name = "habit_records")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HabitRecordEntity {
    
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "habit_type", nullable = false)
    private HabitType habitType;
    
    @Column(nullable = false)
    private boolean completed;
    
    @Column
    private String memo;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static HabitRecordEntity from(HabitRecord habitRecord) {
        return HabitRecordEntity.builder()
                .id(habitRecord.getId())
                .userId(habitRecord.getUserId().getValue())
                .recordDate(habitRecord.getRecordDate())
                .habitType(habitRecord.getHabitType())
                .completed(habitRecord.isCompleted())
                .memo(habitRecord.getMemo())
                .createdAt(habitRecord.getCreatedAt())
                .updatedAt(habitRecord.getUpdatedAt())
                .build();
    }
    
    /**
     * 엔티티를 도메인 모델로 변환
     */
    public HabitRecord toDomain() {
        HabitRecord habitRecord = new HabitRecord(
                UserId.of(this.userId),
                this.recordDate,
                this.habitType,
                this.completed,
                this.memo
        );
        
        setDomainFields(habitRecord);
        return habitRecord;
    }
    
    /**
     * 리플렉션을 사용하여 도메인 객체의 필드 설정
     */
    private void setDomainFields(HabitRecord habitRecord) {
        try {
            var idField = HabitRecord.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(habitRecord, this.id);
            
            var createdAtField = HabitRecord.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(habitRecord, this.createdAt);
            
            var updatedAtField = HabitRecord.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(habitRecord, this.updatedAt);
            
        } catch (Exception e) {
            throw new RuntimeException("도메인 필드 설정 실패", e);
        }
    }
}