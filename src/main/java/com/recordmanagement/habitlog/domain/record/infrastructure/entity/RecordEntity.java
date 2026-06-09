package com.recordmanagement.habitlog.domain.record.infrastructure.entity;

import com.recordmanagement.habitlog.domain.record.domain.model.Record;
import com.recordmanagement.habitlog.domain.record.domain.model.RecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "records", indexes = {
    @Index(name = "idx_user_id_record_date", columnList = "user_id, record_date"),
    @Index(name = "idx_user_id_type", columnList = "user_id, type"),
    @Index(name = "idx_user_id_created_at", columnList = "user_id, created_at"),
    @Index(name = "idx_user_id_record_date_type", columnList = "user_id, record_date, type")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    
    public static RecordEntity from(Record record) {
        return RecordEntity.builder()
            .id(record.getId().value())
            .userId(record.getUserId().getValue())
            .type(record.getType())
            .emotion(record.getEmotion())
            .content(record.getContent())
            .imageUrls(String.join(",", record.getImageUrls()))
            .recordDate(record.getRecordDate())
            .recordTime(record.getRecordTime())
            .createdAt(record.getCreatedAt())
            .updatedAt(record.getUpdatedAt())
            .build();
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
}