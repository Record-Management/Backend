package com.recordmanagement.habitlog.domain.schedule.application.dto;

import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private String scheduleRecordId;
    private String userId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private NotificationType notificationType;
    private Integer notificationCustomHours;
    private Integer notificationCustomMinutes;
    private RepeatType repeatType;
    private LocalDate repeatEndsOn;
    private String location;
    private ScheduleColor color;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleResponse from(ScheduleRecord scheduleRecord) {
        return ScheduleResponse.builder()
                .scheduleRecordId(scheduleRecord.getId().value())
                .userId(scheduleRecord.getUserId().getValue())
                .title(scheduleRecord.getTitle())
                .startDate(scheduleRecord.getStartDate())
                .endDate(scheduleRecord.getEndDate())
                .notificationType(scheduleRecord.getNotificationType())
                .notificationCustomHours(scheduleRecord.getNotificationCustomHours())
                .notificationCustomMinutes(scheduleRecord.getNotificationCustomMinutes())
                .repeatType(scheduleRecord.getRepeatType())
                .repeatEndsOn(scheduleRecord.getRepeatEndsOn())
                .location(scheduleRecord.getLocation())
                .color(scheduleRecord.getColor())
                .memo(scheduleRecord.getMemo())
                .createdAt(scheduleRecord.getCreatedAt())
                .updatedAt(scheduleRecord.getUpdatedAt())
                .build();
    }
}
