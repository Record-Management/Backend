package com.recordmanagement.habitlog.domain.exercise.model;

import java.util.Objects;
import java.util.UUID;

public class ExerciseRecordId {
    
    private final String value;
    
    private ExerciseRecordId(String value) {
        this.value = Objects.requireNonNull(value, "ExerciseRecordId value cannot be null");
    }
    
    public static ExerciseRecordId generate() {
        return new ExerciseRecordId(UUID.randomUUID().toString());
    }
    
    public static ExerciseRecordId from(String value) {
        return new ExerciseRecordId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExerciseRecordId that = (ExerciseRecordId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}