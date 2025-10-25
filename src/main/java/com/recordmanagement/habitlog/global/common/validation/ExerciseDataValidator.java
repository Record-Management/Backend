package com.recordmanagement.habitlog.global.common.validation;

import com.recordmanagement.habitlog.domain.exercise.presentation.dto.CreateExerciseRecordRequest;
import com.recordmanagement.habitlog.domain.exercise.presentation.dto.UpdateExerciseRecordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExerciseDataValidator implements ConstraintValidator<ValidExerciseData, Object> {
    
    @Override
    public void initialize(ValidExerciseData constraintAnnotation) {
        // 초기화 작업 없음
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        
        Integer caloriesBurned = null;
        Integer exerciseTimeMinutes = null;
        Integer stepCount = null;
        Double weight = null;
        
        // CreateExerciseRecordRequest인 경우
        if (value instanceof CreateExerciseRecordRequest request) {
            caloriesBurned = request.getCaloriesBurned();
            exerciseTimeMinutes = request.getExerciseTimeMinutes();
            stepCount = request.getStepCount();
            weight = request.getWeight();
        }
        // UpdateExerciseRecordRequest인 경우  
        else if (value instanceof UpdateExerciseRecordRequest request) {
            caloriesBurned = request.getCaloriesBurned();
            exerciseTimeMinutes = request.getExerciseTimeMinutes();
            stepCount = request.getStepCount();
            weight = request.getWeight();
        }
        else {
            return false;
        }
        
        // 칼로리, 운동시간, 걸음수, 몸무게 중 최소 1개는 0보다 큰 값이어야 함
        boolean hasCalories = caloriesBurned != null && caloriesBurned > 0;
        boolean hasExerciseTime = exerciseTimeMinutes != null && exerciseTimeMinutes > 0;
        boolean hasSteps = stepCount != null && stepCount > 0;
        boolean hasWeight = weight != null && weight > 0;
        
        return hasCalories || hasExerciseTime || hasSteps || hasWeight;
    }
}