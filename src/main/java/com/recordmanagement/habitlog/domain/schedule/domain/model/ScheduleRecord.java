package com.recordmanagement.habitlog.domain.schedule.domain.model;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ScheduleRecord {

    private final ScheduleRecordId id;
    private final UserId userId;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final NotificationType notificationType;
    private final Integer notificationCustomDays;
    private final Integer notificationCustomHours;
    private final RepeatType repeatType;
    private final LocalDate repeatEndsOn;
    private final String location;
    private final ScheduleColor color;
    private final String memo;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ScheduleRecord(ScheduleRecordId id, UserId userId, String title,
                         LocalDate startDate, LocalDate endDate,
                         NotificationType notificationType, Integer notificationCustomDays, Integer notificationCustomHours,
                         RepeatType repeatType, LocalDate repeatEndsOn,
                         String location, ScheduleColor color, String memo,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notificationType = notificationType;
        this.notificationCustomDays = notificationCustomDays;
        this.notificationCustomHours = notificationCustomHours;
        this.repeatType = repeatType;
        this.repeatEndsOn = repeatEndsOn;
        this.location = location;
        this.color = color;
        this.memo = memo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ScheduleRecord create(UserId userId, String title,
                                       LocalDate startDate, LocalDate endDate,
                                       NotificationType notificationType, Integer notificationCustomDays, Integer notificationCustomHours,
                                       RepeatType repeatType, LocalDate repeatEndsOn,
                                       String location, ScheduleColor color, String memo) {
        LocalDateTime now = LocalDateTime.now();
        return new ScheduleRecord(
            ScheduleRecordId.generate(),
            userId,
            title,
            startDate,
            endDate,
            notificationType,
            notificationCustomDays,
            notificationCustomHours,
            repeatType,
            repeatEndsOn,
            location,
            color,
            memo,
            now,
            now
        );
    }

    public ScheduleRecord update(String title, LocalDate startDate, LocalDate endDate,
                                NotificationType notificationType, Integer notificationCustomDays, Integer notificationCustomHours,
                                RepeatType repeatType, LocalDate repeatEndsOn,
                                String location, ScheduleColor color, String memo) {
        return new ScheduleRecord(
            this.id,
            this.userId,
            title,
            startDate,
            endDate,
            notificationType,
            notificationCustomDays,
            notificationCustomHours,
            repeatType,
            repeatEndsOn,
            location,
            color,
            memo,
            this.createdAt,
            LocalDateTime.now()
        );
    }
}
