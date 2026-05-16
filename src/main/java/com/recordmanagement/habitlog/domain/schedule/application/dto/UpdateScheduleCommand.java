package com.recordmanagement.habitlog.domain.schedule.application.dto;

import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleCommand {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private NotificationType notificationType;
    private Integer notificationCustomHours;
    private RepeatType repeatType;
    private LocalDate repeatEndsOn;
    private String location;
    private ScheduleColor color;
    private String memo;
}
