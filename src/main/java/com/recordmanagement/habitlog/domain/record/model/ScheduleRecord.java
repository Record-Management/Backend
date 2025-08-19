package com.recordmanagement.habitlog.domain.record.model;

import com.recordmanagement.habitlog.domain.common.BaseEntity;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 일정 기록 도메인 엔티티
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "일정 기록 도메인 엔티티")
public class ScheduleRecord extends BaseEntity {
    
    @Schema(description = "사용자 ID")
    private UserId userId;
    
    @Schema(description = "일정 제목")
    private String title;
    
    @Schema(description = "일정 타입")
    private ScheduleType scheduleType;
    
    @Schema(description = "시작 날짜")
    private LocalDate startDate;
    
    @Schema(description = "종료 날짜")
    private LocalDate endDate;
    
    @Schema(description = "시작 시간")
    private LocalTime startTime;
    
    @Schema(description = "종료 시간")
    private LocalTime endTime;
    
    @Schema(description = "메모")
    private String memo;
    
    /**
     * 새로운 일정 기록 생성
     */
    public ScheduleRecord(UserId userId, String title, ScheduleType scheduleType,
                         LocalDate startDate, LocalDate endDate, 
                         LocalTime startTime, LocalTime endTime, String memo) {
        this.userId = userId;
        this.title = title;
        this.scheduleType = scheduleType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }
    
    /**
     * 일정 기록 수정
     */
    public void updateSchedule(String title, ScheduleType scheduleType,
                             LocalDate startDate, LocalDate endDate,
                             LocalTime startTime, LocalTime endTime, String memo) {
        this.title = title;
        this.scheduleType = scheduleType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
        this.updateTimestamp();
    }
}