package com.recordmanagement.habitlog.api.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UpdateRecordRequest {
    
    // 기록 타입은 URL 경로로 결정되므로 제거
    
    private String emotion;
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 1000, message = "내용은 1000자 이하로 입력해주세요")
    private String content;
    
    @Size(max = 3, message = "이미지는 최대 3개까지만 첨부할 수 있습니다")
    private List<String> imageUrls;
    
    // Getters and Setters
    // getType() 메서드 제거됨
    
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}