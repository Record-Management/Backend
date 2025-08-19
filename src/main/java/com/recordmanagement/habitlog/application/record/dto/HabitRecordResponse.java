package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.HabitType;
import com.recordmanagement.habitlog.domain.record.model.HabitRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "습관 기록 응답")
public class HabitRecordResponse {
    
    @Schema(description = "습관 기록 ID")
    private final String id;
    
    @Schema(description = "사용자 ID")
    private final String userId;
    
    @Schema(description = "기록 날짜")
    private final LocalDate recordDate;
    
    @Schema(description = "습관 타입")
    private final HabitType habitType;
    
    @Schema(description = "완료 여부")
    private final boolean completed;
    
    @Schema(description = "메모")
    private final String memo;
    
    @Schema(description = "생성 시간")
    private final LocalDateTime createdAt;
    
    @Schema(description = "수정 시간")
    private final LocalDateTime updatedAt;
    
    public static HabitRecordResponse from(HabitRecord habitRecord) {
        return HabitRecordResponse.builder()
                .id(habitRecord.getId())
                .userId(habitRecord.getUserId().getValue())
                .recordDate(habitRecord.getRecordDate())
                .habitType(habitRecord.getHabitType())
                .completed(habitRecord.isCompleted())
                .memo(habitRecord.getMemo())
                .createdAt(habitRecord.getCreatedAt())
                .updatedAt(habitRecord.getUpdatedAt())
                .build();
    }
}