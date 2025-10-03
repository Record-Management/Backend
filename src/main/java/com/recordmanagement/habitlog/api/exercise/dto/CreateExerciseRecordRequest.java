package com.recordmanagement.habitlog.api.exercise.dto;

import com.recordmanagement.habitlog.domain.exercise.model.ExerciseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateExerciseRecordRequest {
    
    @NotNull(message = "운동 종목은 필수입니다")
    private ExerciseType exerciseType;
    
    @PositiveOrZero(message = "소모 칼로리는 0 이상의 값이어야 합니다")
    private Integer caloriesBurned;
    
    @PositiveOrZero(message = "운동 시간은 0 이상의 값이어야 합니다")
    private Integer exerciseTimeMinutes;
    
    @PositiveOrZero(message = "걸음수는 0 이상의 값이어야 합니다")
    private Integer stepCount;
    
    @PositiveOrZero(message = "몸무게는 0 이상의 값이어야 합니다")
    private Double weight;
    
    @NotBlank(message = "나의 기록은 필수입니다")
    private String dailyNote;
    
    @Size(max = 3, message = "이미지는 최대 3개까지만 첨부할 수 있습니다")
    private List<String> imageUrls;
    
    @NotNull(message = "기록 날짜는 필수입니다")
    private String recordDate;
    
    public CreateExerciseRecordRequest() {}
    
    public CreateExerciseRecordRequest(ExerciseType exerciseType, Integer caloriesBurned, 
                                     Integer exerciseTimeMinutes, Integer stepCount, Double weight,
                                     String dailyNote, List<String> imageUrls, String recordDate) {
        this.exerciseType = exerciseType;
        this.caloriesBurned = caloriesBurned;
        this.exerciseTimeMinutes = exerciseTimeMinutes;
        this.stepCount = stepCount;
        this.weight = weight;
        this.dailyNote = dailyNote;
        this.imageUrls = imageUrls;
        this.recordDate = recordDate;
    }
    
    // Getters and Setters
    public ExerciseType getExerciseType() { return exerciseType; }
    public void setExerciseType(ExerciseType exerciseType) { this.exerciseType = exerciseType; }
    
    public Integer getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(Integer caloriesBurned) { this.caloriesBurned = caloriesBurned; }
    
    public Integer getExerciseTimeMinutes() { return exerciseTimeMinutes; }
    public void setExerciseTimeMinutes(Integer exerciseTimeMinutes) { this.exerciseTimeMinutes = exerciseTimeMinutes; }
    
    public Integer getStepCount() { return stepCount; }
    public void setStepCount(Integer stepCount) { this.stepCount = stepCount; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public String getDailyNote() { return dailyNote; }
    public void setDailyNote(String dailyNote) { this.dailyNote = dailyNote; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
}