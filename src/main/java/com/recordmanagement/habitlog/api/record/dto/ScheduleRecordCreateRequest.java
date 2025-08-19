package com.recordmanagement.habitlog.api.record.dto;

import com.recordmanagement.habitlog.domain.record.model.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Schema(description = "일정 기록 생성/수정 요청")
public class ScheduleRecordCreateRequest {
    
    @Schema(description = "제목", required = true)
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @Schema(description = "일정 타입", required = true)
    @NotNull(message = "일정 타입은 필수입니다.")
    private ScheduleType scheduleType;
    
    @Schema(description = "시작 날짜", required = true)
    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;
    
    @Schema(description = "종료 날짜")
    private LocalDate endDate;
    
    @Schema(description = "시작 시간")
    private LocalTime startTime;
    
    @Schema(description = "종료 시간")
    private LocalTime endTime;
    
    @Schema(description = "메모")
    private String memo;
}