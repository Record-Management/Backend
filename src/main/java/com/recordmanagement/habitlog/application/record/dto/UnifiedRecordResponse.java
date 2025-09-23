package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 모든 타입의 기록을 통합해서 표현하는 응답 DTO
 * DAILY, EXERCISE 기록을 하나의 모델로 처리
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UnifiedRecordResponse(
    String id,
    RecordType type,
    
    // 공통 필드
    LocalDate recordDate,
    LocalTime recordTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<String> imageUrls,
    
    // DAILY 기록 필드
    String emotion,
    String content,
    
    // EXERCISE 기록 필드
    String exerciseType,
    Integer exerciseTimeMinutes,
    Integer stepCount,
    Double weight,
    String dailyNote
) {
    
    /**
     * 일상 기록을 통합 응답으로 변환
     */
    public static UnifiedRecordResponse fromRecord(Record record) {
        return new UnifiedRecordResponse(
            record.getId().value(),
            record.getType(),
            record.getRecordDate(),
            record.getRecordTime(),
            record.getCreatedAt(),
            record.getUpdatedAt(),
            record.getImageUrls(),
            record.getEmotion(),
            record.getContent(),
            null, null, null, null, null
        );
    }
    
    /**
     * 운동 기록을 통합 응답으로 변환
     */
    public static UnifiedRecordResponse fromExerciseRecord(ExerciseRecord exerciseRecord) {
        return new UnifiedRecordResponse(
            exerciseRecord.getId().getValue(),
            RecordType.EXERCISE,
            exerciseRecord.getRecordDate(),
            null, // 운동 기록은 recordTime이 없음
            exerciseRecord.getCreatedAt(),
            exerciseRecord.getUpdatedAt(),
            exerciseRecord.getImageUrls(),
            null, null, // 운동 기록은 emotion, content가 없음
            exerciseRecord.getExerciseType() != null ? exerciseRecord.getExerciseType().name() : null,
            exerciseRecord.getExerciseTimeMinutes(),
            exerciseRecord.getStepCount(),
            exerciseRecord.getWeight(),
            exerciseRecord.getDailyNote()
        );
    }
    
    /**
     * 새로운 이미지 URL로 업데이트된 UnifiedRecordResponse 반환
     */
    public UnifiedRecordResponse withUpdatedImageUrls(List<String> newImageUrls) {
        return new UnifiedRecordResponse(
            this.id,
            this.type,
            this.recordDate,
            this.recordTime,
            this.createdAt,
            this.updatedAt,
            newImageUrls,
            this.emotion,
            this.content,
            this.exerciseType,
            this.exerciseTimeMinutes,
            this.stepCount,
            this.weight,
            this.dailyNote
        );
    }
}