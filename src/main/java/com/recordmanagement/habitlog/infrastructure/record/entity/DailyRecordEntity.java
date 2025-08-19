package com.recordmanagement.habitlog.infrastructure.record.entity;

import com.recordmanagement.habitlog.domain.record.model.DailyRecord;
import com.recordmanagement.habitlog.domain.record.model.MoodType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일상 기록 JPA 엔티티
 */
@Entity
@Table(name = "daily_records")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyRecordEntity {
    
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoodType mood;
    
    @Column
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 도메인 모델을 엔티티로 변환
     */
    public static DailyRecordEntity from(DailyRecord dailyRecord) {
        return DailyRecordEntity.builder()
                .id(dailyRecord.getId())
                .userId(dailyRecord.getUserId().getValue())
                .recordDate(dailyRecord.getRecordDate())
                .mood(dailyRecord.getMood())
                .title(dailyRecord.getTitle())
                .content(dailyRecord.getContent())
                .imageUrl(dailyRecord.getImageUrl())
                .createdAt(dailyRecord.getCreatedAt())
                .updatedAt(dailyRecord.getUpdatedAt())
                .build();
    }
    
    /**
     * 엔티티를 도메인 모델로 변환
     */
    public DailyRecord toDomain() {
        DailyRecord dailyRecord = new DailyRecord(
                UserId.of(this.userId),
                this.recordDate,
                this.mood,
                this.title,
                this.content,
                this.imageUrl
        );
        
        setDomainFields(dailyRecord);
        return dailyRecord;
    }
    
    /**
     * 리플렉션을 사용하여 도메인 객체의 필드 설정
     */
    private void setDomainFields(DailyRecord dailyRecord) {
        try {
            var idField = DailyRecord.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(dailyRecord, this.id);
            
            var createdAtField = DailyRecord.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(dailyRecord, this.createdAt);
            
            var updatedAtField = DailyRecord.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(dailyRecord, this.updatedAt);
            
        } catch (Exception e) {
            throw new RuntimeException("도메인 필드 설정 실패", e);
        }
    }
}