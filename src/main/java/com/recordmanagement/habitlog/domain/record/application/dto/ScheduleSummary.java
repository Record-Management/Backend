package com.recordmanagement.habitlog.domain.record.application.dto;

import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "캘린더 일정 요약 정보")
public class ScheduleSummary {

    @Schema(description = "대표 일정명 (첫 번째 일정)", example = "팀 회의")
    private String title;

    @Schema(description = """
            추가 일정 개수 (표시되지 않은 일정 수)
            - 일정 1개: null
            - 일정 2개: 1 ("+1" 표시)
            - 일정 3개: 2 ("+2" 표시)
            """,
            example = "1",
            nullable = true)
    private Integer extraScheduleCount;

    @Schema(description = "대표 일정 색상", example = "BLUE")
    private ScheduleColor color;
}
