package com.recordmanagement.habitlog.domain.schedule.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RepeatType {
    NONE("반복 없음"),
    DAY("매일"),
    WEEK("매주"),
    MONTH("매월"),
    YEAR("매년");

    private final String description;
}
