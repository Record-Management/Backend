package com.recordmanagement.habitlog.domain.record.model;

/**
 * 감정 타입 열거형
 * 
 * - 일상 기록에서 하루의 기분 선택
 */
public enum MoodType {
    VERY_HAPPY("매우 좋음"),
    HAPPY("좋음"),
    NORMAL("보통"),
    SAD("안 좋음"),
    VERY_SAD("매우 안 좋음");
    
    private final String description;
    
    MoodType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}