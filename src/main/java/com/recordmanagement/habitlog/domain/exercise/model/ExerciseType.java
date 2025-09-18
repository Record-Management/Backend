package com.recordmanagement.habitlog.domain.exercise.model;

public enum ExerciseType {
    RUNNING("러닝"),
    GOLF("골프"),
    BASKETBALL("농구"),
    SWIMMING("수영"),
    BASEBALL("야구"),
    YOGA("요가"),
    WEIGHT_TRAINING("웨이트 트레이닝"),
    CYCLING("자전거"),
    SOCCER("축구"),
    TENNIS("테니스");
    
    private final String name;
    
    ExerciseType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}