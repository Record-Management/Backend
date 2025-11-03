package com.recordmanagement.habitlog.domain.habit.presentation.dto;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "습관기록 생성 요청")
public record CreateHabitRecordRequest(
    
    @Schema(description = "습관 종류", example = "WATER_DRINKING")
    @NotNull(message = "습관 종류는 필수입니다")
    HabitType habitType,
    
    @Schema(description = "알림 설정 여부", example = "true")
    @NotNull(message = "알림 설정 여부는 필수입니다")
    Boolean notificationEnabled,
    
    @Schema(description = "알림 시간", example = "09:00")
    LocalTime notificationTime,
    
    @Schema(description = "글쓰기/메모", example = "오늘도 물 2L 마시기 성공!")
    String memo,
    
    @Schema(description = "기록 날짜", example = "2025-10-27")
    @NotNull(message = "기록 날짜는 필수입니다")
    LocalDate recordDate,
    
    @Schema(description = "메인 기록 여부", example = "true")
    Boolean isMainRecord
) {}