package com.recordmanagement.habitlog.domain.record.domain.model;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class Record {
    
    private final RecordId id;
    private final UserId userId;
    private final RecordType type;
    private final String emotion;
    private final String content;
    private final List<String> imageUrls;
    private final LocalDate recordDate;
    private final LocalTime recordTime;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Record(RecordId id, UserId userId, RecordType type, String emotion, 
                 String content, List<String> imageUrls, LocalDate recordDate, LocalTime recordTime,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.emotion = emotion;
        this.content = content;
        this.imageUrls = imageUrls != null ? List.copyOf(imageUrls) : List.of();
        this.recordDate = recordDate;
        this.recordTime = recordTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static Record create(UserId userId, RecordType type, String emotion, 
                              String content, List<String> imageUrls, LocalDate recordDate, LocalTime recordTime) {
        LocalDateTime now = LocalDateTime.now();
        return new Record(
            RecordId.generate(),
            userId,
            type,
            emotion,
            content,
            imageUrls,
            recordDate,
            recordTime,
            now,
            now
        );
    }
    
    public Record updateContent(String newContent) {
        return new Record(
            this.id,
            this.userId,
            this.type,
            this.emotion,
            newContent,
            this.imageUrls,
            this.recordDate,
            this.recordTime,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public Record updateEmotion(String newEmotion) {
        return new Record(
            this.id,
            this.userId,
            this.type,
            newEmotion,
            this.content,
            this.imageUrls,
            this.recordDate,
            this.recordTime,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public Record updateImages(List<String> newImageUrls) {
        return new Record(
            this.id,
            this.userId,
            this.type,
            this.emotion,
            this.content,
            newImageUrls,
            this.recordDate,
            this.recordTime,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    public Record updateType(RecordType newType) {
        return new Record(
            this.id,
            this.userId,
            newType,
            this.emotion,
            this.content,
            this.imageUrls,
            this.recordDate,
            this.recordTime,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    
    // Getters
    public RecordId getId() { return id; }
    public UserId getUserId() { return userId; }
    public RecordType getType() { return type; }
    public String getEmotion() { return emotion; }
    public String getContent() { return content; }
    public List<String> getImageUrls() { return List.copyOf(imageUrls); }
    public LocalDate getRecordDate() { return recordDate; }
    public LocalTime getRecordTime() { return recordTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}