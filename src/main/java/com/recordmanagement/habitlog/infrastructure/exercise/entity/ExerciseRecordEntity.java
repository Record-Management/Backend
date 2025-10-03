package com.recordmanagement.habitlog.infrastructure.exercise.entity;

import com.recordmanagement.habitlog.domain.exercise.model.ExerciseType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exercise_records")
public class ExerciseRecordEntity {
    
    @Id
    @Column(name = "exercise_record_id", length = 36)
    private String exerciseRecordId;
    
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;
    
    @Column(name = "calories_burned")
    private Integer caloriesBurned;
    
    @Column(name = "exercise_time_minutes")
    private Integer exerciseTimeMinutes;
    
    @Column(name = "step_count")
    private Integer stepCount;
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "daily_note", columnDefinition = "TEXT")
    private String dailyNote;
    
    @ElementCollection
    @CollectionTable(
        name = "exercise_record_images",
        joinColumns = @JoinColumn(name = "exercise_record_id", referencedColumnName = "exercise_record_id")
    )
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    protected ExerciseRecordEntity() {}
    
    public ExerciseRecordEntity(String exerciseRecordId, String userId, ExerciseType exerciseType,
                               Integer caloriesBurned, Integer exerciseTimeMinutes, Integer stepCount,
                               Double weight, String dailyNote, List<String> imageUrls,
                               LocalDate recordDate) {
        this.exerciseRecordId = exerciseRecordId;
        this.userId = userId;
        this.exerciseType = exerciseType;
        this.caloriesBurned = caloriesBurned;
        this.exerciseTimeMinutes = exerciseTimeMinutes;
        this.stepCount = stepCount;
        this.weight = weight;
        this.dailyNote = dailyNote;
        this.imageUrls = imageUrls;
        this.recordDate = recordDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getExerciseRecordId() { return exerciseRecordId; }
    public void setExerciseRecordId(String exerciseRecordId) { this.exerciseRecordId = exerciseRecordId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
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
    
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}