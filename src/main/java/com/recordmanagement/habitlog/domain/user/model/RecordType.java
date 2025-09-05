package com.recordmanagement.habitlog.domain.user.model;

/**
 * 사용자가 선택할 수 있는 메인 기록 타입
 * 온보딩 시에만 사용되는 enum
 */
public enum RecordType {
    EXERCISE("운동 기록"),
    DAILY("하루 기록"),
    SCHEDULE("일정 기록"),
    HABIT("습관 기록");
    
    private final String description;
    
    RecordType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}