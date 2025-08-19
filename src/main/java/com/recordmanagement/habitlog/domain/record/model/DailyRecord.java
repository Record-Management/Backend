package com.recordmanagement.habitlog.domain.record.model;

import com.recordmanagement.habitlog.domain.common.BaseEntity;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 일상 기록 도메인 엔티티
 * 
 * 사용자의 일상적인 기록을 관리하는 도메인 모델입니다.
 * 하루에 하나의 일상 기록만 작성 가능하며, 기분, 제목, 내용, 이미지를 포함할 수 있습니다.
 * 
 * 주요 기능:
 * - 일상 기록 생성 및 수정
 * - 기분 상태 기록 (VERY_HAPPY, HAPPY, NORMAL, SAD, VERY_SAD)
 * - 텍스트 기록 (제목, 내용)
 * - 이미지 첨부 (S3 URL)
 * - 생성/수정 시간 자동 관리
 * 
 * 도메인 규칙:
 * - 사용자당 하루에 하나의 일상 기록만 허용
 * - 기분은 필수 입력 항목
 * - 제목과 내용은 선택 사항
 * - 이미지는 S3에 업로드된 URL만 저장
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "일상 기록 도메인 엔티티")
public class DailyRecord extends BaseEntity {
    
    @Schema(description = "사용자 ID")
    private UserId userId;
    
    @Schema(description = "기록 날짜")
    private LocalDate recordDate;
    
    @Schema(description = "기분")
    private MoodType mood;
    
    @Schema(description = "제목")
    private String title;
    
    @Schema(description = "내용")
    private String content;
    
    @Schema(description = "이미지 URL")
    private String imageUrl;
    
    
    /**
     * 새로운 일상 기록 생성
     * 
     * 사용자가 새로운 일상 기록을 작성할 때 호출됩니다.
     * 고유 식별자(UUID)와 생성/수정 시간이 자동으로 설정됩니다.
     * 
     * @param userId 기록을 작성하는 사용자의 ID (필수)
     * @param recordDate 기록 날짜 (필수, 하루에 하나만 허용)
     * @param mood 기분 상태 (필수, MoodType enum 값)
     * @param title 기록 제목 (선택, 최대 255자)
     * @param content 기록 내용 (선택, 최대 10000자)  
     * @param imageUrl 첨부 이미지 S3 URL (선택)
     * 
     * @throws IllegalArgumentException userId가 null인 경우
     * @throws IllegalArgumentException recordDate가 null인 경우
     * @throws IllegalArgumentException mood가 null인 경우
     */
    public DailyRecord(UserId userId, LocalDate recordDate, MoodType mood, 
                      String title, String content, String imageUrl) {
        this.userId = userId;
        this.recordDate = recordDate;
        this.mood = mood;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }
    
    /**
     * 기존 일상 기록 수정
     * 
     * 이미 생성된 일상 기록의 내용을 수정합니다.
     * 수정 시간(updatedAt)이 현재 시간으로 자동 갱신됩니다.
     * ID, 사용자ID, 기록날짜, 생성시간은 변경되지 않습니다.
     * 
     * 비즈니스 규칙:
     * - 기분은 반드시 설정되어야 함 (null 불가)
     * - 제목과 내용은 null 허용 (빈 값으로 수정 가능)
     * - 이미지 URL은 null 허용 (이미지 제거 가능)
     * 
     * @param mood 새로운 기분 상태 (필수, null 불가)
     * @param title 새로운 제목 (선택, null 허용)
     * @param content 새로운 내용 (선택, null 허용)
     * @param imageUrl 새로운 이미지 URL (선택, null 허용)
     * 
     * @throws IllegalArgumentException mood가 null인 경우
     */
    public void updateRecord(MoodType mood, String title, String content, String imageUrl) {
        this.mood = mood;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }
}