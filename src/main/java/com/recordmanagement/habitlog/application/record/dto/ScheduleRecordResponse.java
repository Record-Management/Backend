package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.record.model.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@Schema(description = "일정 기록 응답")
public class ScheduleRecordResponse {
    
    @Schema(description = "일정 기록 ID")
    private final String id;
    
    @Schema(description = "사용자 ID")
    private final String userId;
    
    @Schema(description = "제목")
    private final String title;
    
    @Schema(description = "일정 타입")
    private final ScheduleType scheduleType;
    
    @Schema(description = "시작 날짜")
    private final LocalDate startDate;
    
    @Schema(description = "종료 날짜")
    private final LocalDate endDate;
    
    @Schema(description = "시작 시간")
    private final LocalTime startTime;
    
    @Schema(description = "종료 시간")
    private final LocalTime endTime;
    
    @Schema(description = "메모")
    private final String memo;
    
    @Schema(description = "생성 시간")
    private final LocalDateTime createdAt;
    
    @Schema(description = "수정 시간")
    private final LocalDateTime updatedAt;
    
    public static ScheduleRecordResponse from(ScheduleRecord scheduleRecord) {
        return ScheduleRecordResponse.builder()
                .id(scheduleRecord.getId())
                .userId(scheduleRecord.getUserId().getValue())
                .title(scheduleRecord.getTitle())
                .scheduleType(scheduleRecord.getScheduleType())
                .startDate(scheduleRecord.getStartDate())
                .endDate(scheduleRecord.getEndDate())
                .startTime(scheduleRecord.getStartTime())
                .endTime(scheduleRecord.getEndTime())
                .memo(scheduleRecord.getMemo())
                .createdAt(scheduleRecord.getCreatedAt())
                .updatedAt(scheduleRecord.getUpdatedAt())
                .build();
    }
}