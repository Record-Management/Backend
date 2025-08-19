package com.recordmanagement.habitlog.domain.record.model;

/**
 * 기록 타입 열거형
 * 
 * - 메인 화면에서 보여줄 기록 종류
 */
public enum RecordType {
    EXERCISE("운동 기록"),
    DAILY("일상 기록"),
    SCHEDULE("일정"),
    HABIT("습관");
    
    private final String description;
    
    RecordType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}