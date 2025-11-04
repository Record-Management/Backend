package com.recordmanagement.habitlog.domain.goal.infrastructure.entity;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalId;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 목표 JPA 엔티티
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Entity
@Table(name = "goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GoalEntity {

    @Id
    @Column(name = "goal_id", length = 36)
    private String goalId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20)
    private RecordType recordType;

    @Column(name = "goal_days", nullable = false)
    private int goalDays;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status;

    @Column(name = "completed_days", nullable = false)
    private int completedDays;

    @Column(name = "achievement_rate", nullable = false)
    private double achievementRate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 생성자
     */
    public GoalEntity(String goalId, String userId, RecordType recordType, int goalDays,
                     LocalDate startDate, LocalDate endDate, GoalStatus status,
                     int completedDays, double achievementRate, LocalDateTime createdAt,
                     LocalDateTime completedAt) {
        this.goalId = goalId;
        this.userId = userId;
        this.recordType = recordType;
        this.goalDays = goalDays;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.completedDays = completedDays;
        this.achievementRate = achievementRate;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    /**
     * 도메인 모델로부터 JPA 엔티티 생성
     *
     * @param goal 도메인 모델
     * @return JPA 엔티티
     */
    public static GoalEntity from(Goal goal) {
        return new GoalEntity(
                goal.getId().getValue(),
                goal.getUserId().getValue(),
                goal.getRecordType(),
                goal.getGoalDays(),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.getStatus(),
                goal.getCompletedDays(),
                goal.getAchievementRate(),
                goal.getCreatedAt(),
                goal.getCompletedAt()
        );
    }

    /**
     * JPA 엔티티를 도메인 모델로 변환
     *
     * @return 도메인 모델
     */
    public Goal toDomain() {
        Goal goal = new Goal(
                UserId.from(this.userId),
                this.recordType,
                this.goalDays,
                this.startDate
        );

        // 상태 업데이트 (리플렉션을 통해 private 필드 설정)
        try {
            var idField = goal.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(goal, GoalId.from(this.goalId));

            var statusField = goal.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(goal, this.status);

            var completedDaysField = goal.getClass().getDeclaredField("completedDays");
            completedDaysField.setAccessible(true);
            completedDaysField.set(goal, this.completedDays);

            var achievementRateField = goal.getClass().getDeclaredField("achievementRate");
            achievementRateField.setAccessible(true);
            achievementRateField.set(goal, this.achievementRate);

            var createdAtField = goal.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(goal, this.createdAt);

            var completedAtField = goal.getClass().getDeclaredField("completedAt");
            completedAtField.setAccessible(true);
            completedAtField.set(goal, this.completedAt);

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert GoalEntity to Domain", e);
        }

        return goal;
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
     * 달성률 계산
     *
     * @return 달성률 (%)
     */
    private double calculateAchievementRate() {
        if (goalDays == 0) return 0.0;
        return (double) completedDays / goalDays * 100.0;
    }
}