package com.recordmanagement.habitlog.domain.schedule.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleColor {
    RED("빨강"),
    ORANGE("주황"),
    YELLOW("노랑"),
    GREEN("초록"),
    BLUE("파랑"),
    INDIGO("남색"),
    PINK("핑크"),
    GRAY("회색");

    private final String description;
}
