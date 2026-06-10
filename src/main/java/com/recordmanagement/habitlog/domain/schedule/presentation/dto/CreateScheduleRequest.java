package com.recordmanagement.habitlog.domain.schedule.presentation.dto;

import com.recordmanagement.habitlog.domain.schedule.application.dto.CreateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일정 기록 생성 요청")
public class CreateScheduleRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "일정 제목", example = "매장 점검")
    private String title;

    @NotNull(message = "시작일은 필수입니다.")
    @Schema(description = "시작일", example = "2026-03-21")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @Schema(description = "종료일", example = "2026-03-21")
    private LocalDate endDate;

    @NotNull(message = "알림 타입은 필수입니다.")
    @Schema(description = """
            알림 타입 (NONE, ONE_DAY_BEFORE, TWO_DAYS_BEFORE, CUSTOM)
            - NONE: 알림 없음
            - ONE_DAY_BEFORE: 시작일 1일 전 오전 9:00
            - TWO_DAYS_BEFORE: 시작일 2일 전 오전 9:00
            - CUSTOM: 시작일 당일 사용자 지정 시간 (customHours/customMinutes 필수)
            """,
            example = "ONE_DAY_BEFORE")
    private NotificationType notificationType;

    @Schema(description = "알림 커스텀 시간 (CUSTOM일 때만 필수, 0-23)", example = "9", nullable = true)
    private Integer notificationCustomHours;

    @Schema(description = "알림 커스텀 분 (CUSTOM일 때만 필수, 0-59)", example = "30", nullable = true)
    private Integer notificationCustomMinutes;

    @NotNull(message = "반복 타입은 필수입니다.")
    @Schema(description = "반복 타입", example = "NONE")
    private RepeatType repeatType;

    @Schema(description = "반복 종료일", example = "2026-08-10")
    private LocalDate repeatEndsOn;

    @Schema(description = "위치", example = "도쿄점")
    private String location;

    @NotNull(message = "색상은 필수입니다.")
    @Schema(description = "색상", example = "ORANGE")
    private ScheduleColor color;

    @Schema(description = "메모", example = "오픈 전 냉장고 점검")
    private String memo;

    public CreateScheduleCommand toCommand() {
        return new CreateScheduleCommand(
            title,
            startDate,
            endDate,
            notificationType,
            notificationCustomHours,
            notificationCustomMinutes,
            repeatType,
            repeatEndsOn,
            location,
            color,
            memo
        );
    }
}
