package com.recordmanagement.habitlog.domain.schedule.application.dto;

import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "일정 기록 응답")
public class ScheduleResponse {

    @Schema(description = "일정 ID", example = "schedule-123")
    private String scheduleRecordId;

    @Schema(description = "사용자 ID", example = "user-123")
    private String userId;

    @Schema(description = "일정 제목", example = "매장 점검")
    private String title;

    @Schema(description = "시작일", example = "2026-03-21")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2026-03-21")
    private LocalDate endDate;

    @Schema(description = """
            알림 타입
            - NONE: 알림 없음
            - ONE_DAY_BEFORE: 시작일 1일 전 오전 9:00
            - TWO_DAYS_BEFORE: 시작일 2일 전 오전 9:00
            - CUSTOM: 시작일 당일 사용자 지정 시간
            """,
            example = "ONE_DAY_BEFORE")
    private NotificationType notificationType;

    @Schema(description = "알림 커스텀 시간 (CUSTOM일 때만, 0-23)", example = "9", nullable = true)
    private Integer notificationCustomHours;

    @Schema(description = "알림 커스텀 분 (CUSTOM일 때만, 0-59)", example = "30", nullable = true)
    private Integer notificationCustomMinutes;

    @Schema(description = """
            반복 타입
            - NONE: 반복 없음
            - DAY: 매일
            - WEEK: 매주
            - MONTH: 매월
            - YEAR: 매년
            """,
            example = "NONE")
    private RepeatType repeatType;

    @Schema(description = "반복 종료일", example = "2026-08-10", nullable = true)
    private LocalDate repeatEndsOn;

    @Schema(description = "위치", example = "도쿄점", nullable = true)
    private String location;

    @Schema(description = "색상 (RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, PINK, GRAY)", example = "ORANGE")
    private ScheduleColor color;

    @Schema(description = "메모", example = "오픈 전 냉장고 점검", nullable = true)
    private String memo;

    @Schema(description = "생성 시간", example = "2026-03-20T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2026-03-20T14:30:00")
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
