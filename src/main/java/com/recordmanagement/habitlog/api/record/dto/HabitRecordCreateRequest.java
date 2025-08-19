package com.recordmanagement.habitlog.api.record.dto;

import com.recordmanagement.habitlog.domain.record.model.HabitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "습관 기록 생성/수정 요청")
public class HabitRecordCreateRequest {
    
    @Schema(description = "습관 타입", required = true)
    @NotNull(message = "습관 타입은 필수입니다.")
    private HabitType habitType;
    
    @Schema(description = "완료 여부", defaultValue = "false")
    private boolean completed;
    
    @Schema(description = "메모")
    private String memo;
}