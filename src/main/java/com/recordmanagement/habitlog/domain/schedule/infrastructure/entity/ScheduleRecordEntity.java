package com.recordmanagement.habitlog.domain.schedule.infrastructure.entity;

import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_records", indexes = {
    @Index(name = "idx_user_id_start_date", columnList = "user_id, start_date"),
    @Index(name = "idx_user_id_end_date", columnList = "user_id, end_date"),
    @Index(name = "idx_user_id_repeat_type", columnList = "user_id, repeat_type"),
    @Index(name = "idx_user_id_created_at", columnList = "user_id, created_at")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleRecordEntity {

    @Id
    @Column(name = "schedule_record_id", length = 36)
    private String scheduleRecordId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType;

    @Column(name = "notification_custom_hours")
    private Integer notificationCustomHours;

    @Column(name = "notification_custom_minutes")
    private Integer notificationCustomMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false, length = 10)
    private RepeatType repeatType;

    @Column(name = "repeat_ends_on")
    private LocalDate repeatEndsOn;

    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 10)
    private ScheduleColor color;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ScheduleRecordEntity from(ScheduleRecord scheduleRecord) {
        return ScheduleRecordEntity.builder()
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

    public ScheduleRecord toDomain() {
        return new ScheduleRecord(
            ScheduleRecordId.from(this.scheduleRecordId),
            UserId.of(this.userId),
            this.title,
            this.startDate,
            this.endDate,
            this.notificationType,
            this.notificationCustomHours,
            this.notificationCustomMinutes,
            this.repeatType,
            this.repeatEndsOn,
            this.location,
            this.color,
            this.memo,
            this.createdAt,
            this.updatedAt
        );
    }
}
