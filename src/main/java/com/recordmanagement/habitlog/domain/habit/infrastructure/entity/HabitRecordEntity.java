package com.recordmanagement.habitlog.domain.habit.infrastructure.entity;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "habit_records")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HabitRecordEntity {
    
    @Id
    @Column(name = "habit_record_id", length = 36)
    private String habitRecordId;
    
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "habit_type", nullable = false)
    private HabitType habitType;
    
    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;
    
    @Column(name = "notification_time")
    private LocalTime notificationTime;
    
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;
    
    @Column(name = "is_main_record", nullable = false)
    private boolean isMainRecord;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}