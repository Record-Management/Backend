package com.recordmanagement.habitlog.domain.record.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreationLimitsResponse {
    private boolean canCreateRecord;    // 기록 생성 가능 여부 (recordDate 기준 DAILY+EXERCISE+HABIT 합계 < 2)
    private boolean canCreateSchedule;  // 일정 생성 가능 여부 (createdAt 기준 일정 개수 < 2)
}
