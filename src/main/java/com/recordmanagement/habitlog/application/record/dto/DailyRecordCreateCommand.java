package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.MoodType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 일상 기록 생성 커맨드
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "일상 기록 생성 커맨드")
public class DailyRecordCreateCommand {
    
    @Schema(description = "사용자 ID")
    private final String userId;
    
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
}