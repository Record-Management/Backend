package com.recordmanagement.habitlog.domain.exercise.presentation.dto;

import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseType;
import com.recordmanagement.habitlog.global.common.validation.ValidExerciseData;
import com.recordmanagement.habitlog.global.common.validation.ValidRecordDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ValidExerciseData
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @ValidRecordDate
    private String recordDate;
    
    @NotNull(message = "기록 시간은 필수입니다")
    private String recordTime;
}