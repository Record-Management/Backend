package com.recordmanagement.habitlog.domain.record.model;

/**
 * 일정 타입 열거형
 */
public enum ScheduleType {
    ALL_DAY("하루 종일"),
    TIMED("시간 지정"),
    PERIOD("기간");
    
    private final String description;
    
    ScheduleType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}