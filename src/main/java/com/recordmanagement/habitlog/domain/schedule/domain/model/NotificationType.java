package com.recordmanagement.habitlog.domain.schedule.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    NONE("알림 없음"),
    ONE_DAY_BEFORE("1일 전 오전 9시"),
    TWO_DAYS_BEFORE("2일 전 오전 9시"),
    CUSTOM("사용자 지정");

    private final String description;
}
