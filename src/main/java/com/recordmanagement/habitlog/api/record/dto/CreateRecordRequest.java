package com.recordmanagement.habitlog.api.record.dto;

import com.recordmanagement.habitlog.domain.user.model.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CreateRecordRequest {
    
    @NotNull(message = "기록 타입은 필수입니다")
    private RecordType type;
    
    private String emotion;
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 1000, message = "내용은 1000자 이하로 입력해주세요")
    private String content;
    
    @Size(max = 3, message = "이미지는 최대 3개까지만 첨부할 수 있습니다")
    private List<String> imageUrls;
    
    @NotNull(message = "기록 날짜는 필수입니다")
    private LocalDate recordDate;
    
    @NotNull(message = "기록 시간은 필수입니다")
    private LocalTime recordTime;
    
    // Getters and Setters
    public RecordType getType() { return type; }
    public void setType(RecordType type) { this.type = type; }
    
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    
    public LocalTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalTime recordTime) { this.recordTime = recordTime; }
}