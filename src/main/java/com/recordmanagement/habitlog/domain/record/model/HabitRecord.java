package com.recordmanagement.habitlog.domain.record.model;

import com.recordmanagement.habitlog.domain.common.BaseEntity;
import com.recordmanagement.habitlog.domain.record.model.HabitType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 습관 기록 도메인 엔티티
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "습관 기록 도메인 엔티티")
public class HabitRecord extends BaseEntity {
    
    @Schema(description = "사용자 ID")
    private UserId userId;
    
    @Schema(description = "기록 날짜")
    private LocalDate recordDate;
    
    @Schema(description = "습관 타입")
    private HabitType habitType;
    
    @Schema(description = "완료 여부")
    private boolean completed;
    
    @Schema(description = "메모")
    private String memo;
    
    /**
     * 새로운 습관 기록 생성
     */
    public HabitRecord(UserId userId, LocalDate recordDate, HabitType habitType, 
                      boolean completed, String memo) {
        this.userId = userId;
        this.recordDate = recordDate;
        this.habitType = habitType;
        this.completed = completed;
        this.memo = memo;
    }
    
    /**
     * 습관 기록 수정
     */
    public void updateRecord(boolean completed, String memo) {
        this.completed = completed;
        this.memo = memo;
        this.updateTimestamp();
    }
    
    /**
     * 완료 상태 토글
     */
    public void toggleCompletion() {
        this.completed = !this.completed;
        this.updateTimestamp();
    }
}