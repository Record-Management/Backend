package com.recordmanagement.habitlog.domain.exercise.domain.model;

import lombok.Value;
import java.util.Objects;
import java.util.UUID;

@Value
public class ExerciseRecordId {
    
    String value;
    
    private ExerciseRecordId(String value) {
        this.value = Objects.requireNonNull(value, "ExerciseRecordId value cannot be null");
    }
    
    public static ExerciseRecordId generate() {
        return new ExerciseRecordId(UUID.randomUUID().toString());
    }
    
    public static ExerciseRecordId from(String value) {
        return new ExerciseRecordId(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}