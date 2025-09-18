package com.recordmanagement.habitlog.domain.exercise.model;

import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ExerciseRecord {
    
    private final ExerciseRecordId id;
    private final UserId userId;
    private final ExerciseType exerciseType;
    private final Integer caloriesBurned;
    private final Integer exerciseTimeMinutes;
    private final Integer stepCount;
    private final Double weight;
    private final String dailyNote;
    private final List<String> imageUrls;
    private final LocalDate recordDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ExerciseRecord(ExerciseRecordId id, UserId userId, ExerciseType exerciseType,
                         Integer caloriesBurned, Integer exerciseTimeMinutes, Integer stepCount,
                         Double weight, String dailyNote, List<String> imageUrls,
                         LocalDate recordDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.exerciseType = exerciseType;
        this.caloriesBurned = caloriesBurned;
        this.exerciseTimeMinutes = exerciseTimeMinutes;
        this.stepCount = stepCount;
        this.weight = weight;
        this.dailyNote = dailyNote;
        this.imageUrls = imageUrls != null ? List.copyOf(imageUrls) : List.of();
        this.recordDate = recordDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static ExerciseRecord create(UserId userId, ExerciseType exerciseType,
                                      Integer caloriesBurned, Integer exerciseTimeMinutes,
                                      Integer stepCount, Double weight, String dailyNote,
                                      List<String> imageUrls, LocalDate recordDate) {
        LocalDateTime now = LocalDateTime.now();
        return new ExerciseRecord(
            ExerciseRecordId.generate(),
            userId,
            exerciseType,
            caloriesBurned,
            exerciseTimeMinutes,
            stepCount,
            weight,
            dailyNote,
            imageUrls,
            recordDate,
            now,
            now
        );
    }
    
    public ExerciseRecord updateExerciseDetails(ExerciseType exerciseType, Integer caloriesBurned,
                                              Integer exerciseTimeMinutes, Integer stepCount) {
        return new ExerciseRecord(
            this.id,
            this.userId,
            exerciseType,
            caloriesBurned,
            exerciseTimeMinutes,
            stepCount,
            this.weight,
            this.dailyNote,
            this.imageUrls,
            this.recordDate,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public ExerciseRecord updateWeight(Double weight) {
        return new ExerciseRecord(
            this.id,
            this.userId,
            this.exerciseType,
            this.caloriesBurned,
            this.exerciseTimeMinutes,
            this.stepCount,
            weight,
            this.dailyNote,
            this.imageUrls,
            this.recordDate,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public ExerciseRecord updateDailyNote(String dailyNote) {
        return new ExerciseRecord(
            this.id,
            this.userId,
            this.exerciseType,
            this.caloriesBurned,
            this.exerciseTimeMinutes,
            this.stepCount,
            this.weight,
            dailyNote,
            this.imageUrls,
            this.recordDate,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public ExerciseRecord updateImages(List<String> imageUrls) {
        return new ExerciseRecord(
            this.id,
            this.userId,
            this.exerciseType,
            this.caloriesBurned,
            this.exerciseTimeMinutes,
            this.stepCount,
            this.weight,
            this.dailyNote,
            imageUrls,
            this.recordDate,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    // Getters
    public ExerciseRecordId getId() { return id; }
    public UserId getUserId() { return userId; }
    public ExerciseType getExerciseType() { return exerciseType; }
    public Integer getCaloriesBurned() { return caloriesBurned; }
    public Integer getExerciseTimeMinutes() { return exerciseTimeMinutes; }
    public Integer getStepCount() { return stepCount; }
    public Double getWeight() { return weight; }
    public String getDailyNote() { return dailyNote; }
    public List<String> getImageUrls() { return List.copyOf(imageUrls); }
    public LocalDate getRecordDate() { return recordDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}