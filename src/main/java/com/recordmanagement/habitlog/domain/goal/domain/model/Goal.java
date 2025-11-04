package com.recordmanagement.habitlog.domain.goal.domain.model;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 목표 도메인 엔티티
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal {

    private GoalId id;
    private UserId userId;
    private RecordType recordType;
    private int goalDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private GoalStatus status;
    private int completedDays;
    private double achievementRate;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    /**
     * 새로운 목표 생성
     *
     * @param userId 사용자 ID
     * @param recordType 기록 타입
     * @param goalDays 목표일수 (10, 20, 30)
     * @param startDate 시작일
     */
    public Goal(UserId userId, RecordType recordType, int goalDays, LocalDate startDate) {
        this.id = GoalId.generate();
        this.userId = userId;
        this.recordType = recordType;
        this.goalDays = goalDays;
        this.startDate = startDate;
        this.endDate = startDate.plusDays(goalDays - 1);
        this.status = GoalStatus.IN_PROGRESS;
        this.completedDays = 0;
        this.achievementRate = 0.0;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 완료일수 업데이트
     *
     * @param completedDays 완료일수
     */
    public void updateCompletedDays(int completedDays) {
        this.completedDays = completedDays;
        this.achievementRate = calculateAchievementRate();
    }

    /**
     * 목표 완료 처리
     */
    public void complete() {
        this.status = GoalStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 목표 포기 처리
     */
    public void abandon() {
        this.status = GoalStatus.ABANDONED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 목표 달성률 계산
     *
     * @return 달성률 (%)
     */
    public double calculateAchievementRate() {
        if (goalDays == 0) return 0.0;
        return (double) completedDays / goalDays * 100.0;
    }

    /**
     * 현재 나무 단계 조회
     *
     * @return 나무 단계
     */
    public TreeStage getCurrentTreeStage() {
        return TreeStage.calculateStage(goalDays, completedDays);
    }

    /**
     * 목표 진행 중 여부 확인
     *
     * @return true: 진행중, false: 완료/포기
     */
    public boolean isInProgress() {
        return status == GoalStatus.IN_PROGRESS;
    }

    /**
     * 목표 완료 여부 확인
     *
     * @return true: 완료, false: 진행중/포기
     */
    public boolean isCompleted() {
        return status == GoalStatus.COMPLETED;
    }

    /**
     * 목표 기간 종료 여부 확인
     *
     * @return true: 기간 종료, false: 기간 내
     */
    public boolean isPeriodEnded() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * 누적 달성 횟수에 포함되는지 확인
     * (완료된 목표만 포함)
     *
     * @return true: 포함, false: 미포함
     */
    public boolean shouldCountForCumulative() {
        return status == GoalStatus.COMPLETED;
    }
}