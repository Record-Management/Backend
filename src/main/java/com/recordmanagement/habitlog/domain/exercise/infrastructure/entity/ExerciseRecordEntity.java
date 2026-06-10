package com.recordmanagement.habitlog.domain.exercise.infrastructure.entity;

import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "exercise_records", indexes = {
    @Index(name = "idx_user_id_record_date", columnList = "user_id, record_date")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    
    @Column(name = "record_time", nullable = false)
    private LocalTime recordTime;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}