package com.recordmanagement.habitlog.infrastructure.habit.entity;

import com.recordmanagement.habitlog.domain.habit.model.HabitType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "habit_records")
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
    
    protected HabitRecordEntity() {}
    
    public HabitRecordEntity(String habitRecordId, String userId, HabitType habitType,
                            boolean notificationEnabled, LocalTime notificationTime, String memo,
                            LocalDate recordDate, boolean isCompleted, boolean isMainRecord) {
        this.habitRecordId = habitRecordId;
        this.userId = userId;
        this.habitType = habitType;
        this.notificationEnabled = notificationEnabled;
        this.notificationTime = notificationTime;
        this.memo = memo;
        this.recordDate = recordDate;
        this.isCompleted = isCompleted;
        this.isMainRecord = isMainRecord;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getHabitRecordId() { return habitRecordId; }
    public void setHabitRecordId(String habitRecordId) { this.habitRecordId = habitRecordId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public HabitType getHabitType() { return habitType; }
    public void setHabitType(HabitType habitType) { this.habitType = habitType; }
    
    public boolean isNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
    
    public LocalTime getNotificationTime() { return notificationTime; }
    public void setNotificationTime(LocalTime notificationTime) { this.notificationTime = notificationTime; }
    
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }
    
    public boolean isMainRecord() { return isMainRecord; }
    public void setMainRecord(boolean mainRecord) { this.isMainRecord = mainRecord; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}