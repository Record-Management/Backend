package com.recordmanagement.habitlog.domain.habit.model;

import java.util.Objects;
import java.util.UUID;

public class HabitRecordId {
    
    private final String value;
    
    private HabitRecordId(String value) {
        this.value = Objects.requireNonNull(value, "HabitRecordId value cannot be null");
    }
    
    public static HabitRecordId generate() {
        return new HabitRecordId(UUID.randomUUID().toString());
    }
    
    public static HabitRecordId from(String value) {
        return new HabitRecordId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitRecordId that = (HabitRecordId) o;
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