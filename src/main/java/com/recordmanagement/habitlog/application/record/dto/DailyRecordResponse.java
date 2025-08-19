package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.DailyRecord;
import com.recordmanagement.habitlog.domain.record.model.MoodType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일상 기록 응답 DTO
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "일상 기록 응답")
public class DailyRecordResponse {
    
    @Schema(description = "일상 기록 ID")
    private final String id;
    
    @Schema(description = "기록 날짜")
    private final LocalDate recordDate;
    
    @Schema(description = "기분")
    private final MoodType mood;
    
    @Schema(description = "제목")
    private final String title;
    
    @Schema(description = "내용")
    private final String content;
    
    @Schema(description = "이미지 URL")
    private final String imageUrl;
    
    @Schema(description = "생성 시간")
    private final LocalDateTime createdAt;
    
    @Schema(description = "수정 시간")
    private final LocalDateTime updatedAt;
    
    public static DailyRecordResponse from(DailyRecord dailyRecord) {
        return new DailyRecordResponse(
                dailyRecord.getId(),
                dailyRecord.getRecordDate(),
                dailyRecord.getMood(),
                dailyRecord.getTitle(),
                dailyRecord.getContent(),
                dailyRecord.getImageUrl(),
                dailyRecord.getCreatedAt(),
                dailyRecord.getUpdatedAt()
        );
    }
}