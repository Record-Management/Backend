package com.recordmanagement.habitlog.domain.exercise.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
}