package com.recordmanagement.habitlog.infrastructure.record.entity;

import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "records", indexes = {
    @Index(name = "idx_user_id_record_date", columnList = "user_id, record_date"),
    @Index(name = "idx_user_id_type", columnList = "user_id, type"),
    @Index(name = "idx_user_id_created_at", columnList = "user_id, created_at")
})
public class RecordEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RecordType type;
    
    @Column(name = "emotion")
    private String emotion;
    
    @Column(name = "content", nullable = false, length = 1000)
    private String content;
    
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Column(name = "record_time")
    private LocalTime recordTime;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    protected RecordEntity() {
    }
    
    public RecordEntity(String id, String userId, RecordType type, String emotion,
                       String content, String imageUrls, LocalDate recordDate, LocalTime recordTime,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.emotion = emotion;
        this.content = content;
        this.imageUrls = imageUrls;
        this.recordDate = recordDate;
        this.recordTime = recordTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public static RecordEntity from(Record record) {
        return new RecordEntity(
            record.getId().value(),
            record.getUserId().getValue(),
            record.getType(),
            record.getEmotion(),
            record.getContent(),
            String.join(",", record.getImageUrls()),
            record.getRecordDate(),
            record.getRecordTime(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
    
    public Record toDomain() {
        List<String> imageUrlList = imageUrls == null || imageUrls.trim().isEmpty() 
            ? List.of() 
            : Arrays.asList(imageUrls.split(","));
            
        return new Record(
            RecordId.from(this.id),
            UserId.of(this.userId),
            this.type,
            this.emotion,
            this.content,
            imageUrlList,
            this.recordDate,
            this.recordTime,
            this.createdAt,
            this.updatedAt
        );
    }
    
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public RecordType getType() { return type; }
    public String getEmotion() { return emotion; }
    public String getContent() { return content; }
    public String getImageUrls() { return imageUrls; }
    public LocalDate getRecordDate() { return recordDate; }
    public LocalTime getRecordTime() { return recordTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}