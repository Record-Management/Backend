package com.recordmanagement.habitlog.api.record.dto;

import com.recordmanagement.habitlog.domain.record.model.MoodType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "일상 기록 생성/수정 요청")
public class DailyRecordCreateRequest {
    
    @Schema(description = "기분", required = true)
    @NotNull(message = "기분은 필수입니다.")
    private MoodType mood;
    
    @Schema(description = "제목")
    private String title;
    
    @Schema(description = "내용")
    private String content;
    
    @Schema(description = "이미지 URL")
    private String imageUrl;
}