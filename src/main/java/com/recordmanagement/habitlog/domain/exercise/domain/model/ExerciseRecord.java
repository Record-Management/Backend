package com.recordmanagement.habitlog.domain.exercise.domain.model;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@With
@AllArgsConstructor(access = AccessLevel.PACKAGE)
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
    private final LocalTime recordTime;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    
    public static ExerciseRecord create(UserId userId, ExerciseType exerciseType,
                                      Integer caloriesBurned, Integer exerciseTimeMinutes,
                                      Integer stepCount, Double weight, String dailyNote,
                                      List<String> imageUrls, LocalDate recordDate, LocalTime recordTime) {
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
            recordTime,
            now,
            now
        );
    }
    
    public static ExerciseRecord restore(ExerciseRecordId id, UserId userId, ExerciseType exerciseType,
                                       Integer caloriesBurned, Integer exerciseTimeMinutes,
                                       Integer stepCount, Double weight, String dailyNote,
                                       List<String> imageUrls, LocalDate recordDate, LocalTime recordTime,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ExerciseRecord(
            id, userId, exerciseType, caloriesBurned, exerciseTimeMinutes,
            stepCount, weight, dailyNote, imageUrls, recordDate, recordTime,
            createdAt, updatedAt
        );
    }
    
    public ExerciseRecord updateExerciseDetails(ExerciseType exerciseType, Integer caloriesBurned,
                                              Integer exerciseTimeMinutes, Integer stepCount) {
        return this.withExerciseType(exerciseType)
                  .withCaloriesBurned(caloriesBurned)
                  .withExerciseTimeMinutes(exerciseTimeMinutes)
                  .withStepCount(stepCount)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public ExerciseRecord updateWeight(Double weight) {
        return this.withWeight(weight)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public ExerciseRecord updateDailyNote(String dailyNote) {
        return this.withDailyNote(dailyNote)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public ExerciseRecord updateImages(List<String> imageUrls) {
        return this.withImageUrls(imageUrls)
                  .withUpdatedAt(LocalDateTime.now());
    }
    
    public ExerciseRecord updateRecordTime(LocalTime recordTime) {
        return this.withRecordTime(recordTime)
                  .withUpdatedAt(LocalDateTime.now());
    }

    // getImageUrls()는 방어적 복사가 필요하므로 별도 구현
    public List<String> getImageUrls() { 
        return List.copyOf(imageUrls); 
    }
}