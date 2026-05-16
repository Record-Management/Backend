package com.recordmanagement.habitlog.domain.record.application.dto;

import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSummary {
    private String title;      // 대표 일정명
    private Integer size;      // 일정 개수
    private ScheduleColor color; // 대표 일정 색상
}
