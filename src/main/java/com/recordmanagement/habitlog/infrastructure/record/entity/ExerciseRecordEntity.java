package com.recordmanagement.habitlog.infrastructure.record.entity;

import com.recordmanagement.habitlog.domain.record.model.ExerciseType;
import com.recordmanagement.habitlog.domain.record.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 운동 기록 JPA 엔티티
 */
@Entity
@Table(name = "exercise_records")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExerciseRecordEntity {
    
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;
    
    @Column
    private Integer calories;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column
    private Double weight;
    
    @Column
    private Integer steps;
    
    @Column
    private String memo;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static ExerciseRecordEntity from(ExerciseRecord exerciseRecord) {
        return ExerciseRecordEntity.builder()
                .id(exerciseRecord.getId())
                .userId(exerciseRecord.getUserId().getValue())
                .recordDate(exerciseRecord.getRecordDate())
                .exerciseType(exerciseRecord.getExerciseType())
                .calories(exerciseRecord.getCalories())
                .durationMinutes(exerciseRecord.getDurationMinutes())
                .weight(exerciseRecord.getWeight())
                .steps(exerciseRecord.getSteps())
                .memo(exerciseRecord.getMemo())
                .createdAt(exerciseRecord.getCreatedAt())
                .updatedAt(exerciseRecord.getUpdatedAt())
                .build();
    }
    
    /**
     * 엔티티를 도메인 모델로 변환
     */
    public ExerciseRecord toDomain() {
        ExerciseRecord exerciseRecord = new ExerciseRecord(
                UserId.of(this.userId),
                this.recordDate,
                this.exerciseType,
                this.calories,
                this.durationMinutes,
                this.weight,
                this.steps,
                this.memo
        );
        
        setDomainFields(exerciseRecord);
        return exerciseRecord;
    }
    
    /**
     * 리플렉션을 사용하여 도메인 객체의 필드 설정
     */
    private void setDomainFields(ExerciseRecord exerciseRecord) {
        try {
            var idField = ExerciseRecord.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(exerciseRecord, this.id);
            
            var createdAtField = ExerciseRecord.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(exerciseRecord, this.createdAt);
            
            var updatedAtField = ExerciseRecord.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(exerciseRecord, this.updatedAt);
            
        } catch (Exception e) {
            throw new RuntimeException("도메인 필드 설정 실패", e);
        }
    }
}