package com.recordmanagement.habitlog.domain.habit.model;

import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class HabitRecord {
    
    private final HabitRecordId id;
    private final UserId userId;
    private final HabitType habitType;
    private final boolean notificationEnabled;
    private final LocalTime notificationTime;
    private final String memo;
    private final LocalDate recordDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public HabitRecord(HabitRecordId id, UserId userId, HabitType habitType,
                      boolean notificationEnabled, LocalTime notificationTime, String memo,
                      LocalDate recordDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.habitType = habitType;
        this.notificationEnabled = notificationEnabled;
        this.notificationTime = notificationTime;
        this.memo = memo;
        this.recordDate = recordDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static HabitRecord create(UserId userId, HabitType habitType,
                                   boolean notificationEnabled, LocalTime notificationTime,
                                   String memo, LocalDate recordDate) {
        LocalDateTime now = LocalDateTime.now();
        return new HabitRecord(
            HabitRecordId.generate(),
            userId,
            habitType,
            notificationEnabled,
            notificationTime,
            memo,
            recordDate,
            now,
            now
        );
    }
    
    public HabitRecord updateHabitType(HabitType habitType) {
        return new HabitRecord(
            this.id,
            this.userId,
            habitType,
            this.notificationEnabled,
            this.notificationTime,
            this.memo,
            this.recordDate,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public HabitRecord updateNotificationSettings(boolean notificationEnabled, LocalTime notificationTime) {
        return new HabitRecord(
            this.id,
            this.userId,
            this.habitType,
            notificationEnabled,
            notificationTime,
            this.memo,
            this.recordDate,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public HabitRecord updateMemo(String memo) {
        return new HabitRecord(
            this.id,
            this.userId,
            this.habitType,
            this.notificationEnabled,
            this.notificationTime,
            memo,
            this.recordDate,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    // Getters
    public HabitRecordId getId() { return id; }
    public UserId getUserId() { return userId; }
    public HabitType getHabitType() { return habitType; }
    public boolean isNotificationEnabled() { return notificationEnabled; }
    public LocalTime getNotificationTime() { return notificationTime; }
    public String getMemo() { return memo; }
    public LocalDate getRecordDate() { return recordDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}