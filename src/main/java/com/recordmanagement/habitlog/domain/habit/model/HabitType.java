package com.recordmanagement.habitlog.domain.habit.model;

public enum HabitType {
    WATER_DRINKING("물 마시기"),
    WALKING("산책"),
    READING("독서"),
    SAVING("저축"),
    TAKE_MEDICINE("약 챙겨먹기"),
    EARLY_RISING("일찍 일어나기"),
    STRETCHING("스트레칭"),
    EXERCISE("운동"),
    NO_DRINKING("금주"),
    NO_SMOKING("금연");
    
    private final String description;
    
    HabitType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}