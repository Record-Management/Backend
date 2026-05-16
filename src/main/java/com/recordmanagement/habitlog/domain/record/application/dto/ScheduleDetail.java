package com.recordmanagement.habitlog.domain.record.application.dto;

import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetail {
    private String scheduleId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private ScheduleColor color;
    private String memo;

    public static ScheduleDetail from(ScheduleRecord schedule) {
        return ScheduleDetail.builder()
                .scheduleId(schedule.getId().value())
                .title(schedule.getTitle())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .color(schedule.getColor())
                .memo(schedule.getMemo())
                .build();
    }
}
