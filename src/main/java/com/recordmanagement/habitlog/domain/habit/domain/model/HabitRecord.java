package com.recordmanagement.habitlog.domain.habit.domain.model;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@With
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class HabitRecord {
    
    private final HabitRecordId id;
    private final UserId userId;
    private final HabitType habitType;
    private final boolean notificationEnabled;
    private final LocalTime notificationTime;
    private final String memo;
    private final LocalDate recordDate;
    private final boolean isCompleted;
    private final boolean isMainRecord;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
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
            false, // 기본값: 미완료
            true,  // 기본값: 메인 기록
            now,
            now
        );
    }
    
    public static HabitRecord restore(HabitRecordId id, UserId userId, HabitType habitType,
                                    boolean notificationEnabled, LocalTime notificationTime,
                                    String memo, LocalDate recordDate, boolean isCompleted,
                                    boolean isMainRecord, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new HabitRecord(
            id, userId, habitType, notificationEnabled, notificationTime,
            memo, recordDate, isCompleted, isMainRecord, createdAt, updatedAt
        );
    }
    
    public HabitRecord updateHabitType(HabitType habitType) {
        return this.withHabitType(habitType)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public HabitRecord updateNotificationSettings(boolean notificationEnabled, LocalTime notificationTime) {
        return this.withNotificationEnabled(notificationEnabled)
                  .withNotificationTime(notificationTime)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public HabitRecord updateMemo(String memo) {
        return this.withMemo(memo)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public HabitRecord updateCompletionStatus(boolean isCompleted) {
        return this.withCompleted(isCompleted)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public HabitRecord updateMainRecordStatus(boolean isMainRecord) {
        return this.withMainRecord(isMainRecord)
                  .withUpdatedAt(LocalDateTime.now());
    }

}