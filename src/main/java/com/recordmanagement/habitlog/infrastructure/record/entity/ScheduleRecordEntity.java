package com.recordmanagement.habitlog.infrastructure.record.entity;

import com.recordmanagement.habitlog.domain.record.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.record.model.ScheduleType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 일정 기록 JPA 엔티티
 */
@Entity
@Table(name = "schedule_records")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleRecordEntity {
    
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String title;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column
    private String memo;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static ScheduleRecordEntity from(ScheduleRecord scheduleRecord) {
        return ScheduleRecordEntity.builder()
                .id(scheduleRecord.getId())
                .userId(scheduleRecord.getUserId().getValue())
                .title(scheduleRecord.getTitle())
                .scheduleType(scheduleRecord.getScheduleType())
                .startDate(scheduleRecord.getStartDate())
                .endDate(scheduleRecord.getEndDate())
                .startTime(scheduleRecord.getStartTime())
                .endTime(scheduleRecord.getEndTime())
                .memo(scheduleRecord.getMemo())
                .createdAt(scheduleRecord.getCreatedAt())
                .updatedAt(scheduleRecord.getUpdatedAt())
                .build();
    }
    
    /**
     * 엔티티를 도메인 모델로 변환
     */
    public ScheduleRecord toDomain() {
        ScheduleRecord scheduleRecord = new ScheduleRecord(
                UserId.of(this.userId),
                this.title,
                this.scheduleType,
                this.startDate,
                this.endDate,
                this.startTime,
                this.endTime,
                this.memo
        );
        
        setDomainFields(scheduleRecord);
        return scheduleRecord;
    }
    
    /**
     * 리플렉션을 사용하여 도메인 객체의 필드 설정
     */
    private void setDomainFields(ScheduleRecord scheduleRecord) {
        try {
            var idField = ScheduleRecord.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(scheduleRecord, this.id);
            
            var createdAtField = ScheduleRecord.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(scheduleRecord, this.createdAt);
            
            var updatedAtField = ScheduleRecord.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(scheduleRecord, this.updatedAt);
            
        } catch (Exception e) {
            throw new RuntimeException("도메인 필드 설정 실패", e);
        }
    }
}