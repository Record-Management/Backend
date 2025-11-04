package com.recordmanagement.habitlog.domain.goal.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 나무 성장 단계 열거형
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum TreeStage {

    STAGE_1(1, "씨앗/새싹"),
    STAGE_2(2, "어린나무"),
    STAGE_3(3, "성숙한나무"),
    STAGE_4(4, "완성된나무");

    private final int stage;
    private final String description;

    /**
     * 목표일수와 완료일수를 기반으로 나무 단계 계산
     *
     * @param goalDays 목표일수 (10, 20, 30)
     * @param completedDays 완료일수
     * @return 나무 단계
     */
    public static TreeStage calculateStage(int goalDays, int completedDays) {
        if (completedDays <= 0) {
            return STAGE_1;
        }

        return switch (goalDays) {
            case 10 -> {
                if (completedDays <= 2) yield STAGE_1;
                else if (completedDays <= 5) yield STAGE_2;
                else if (completedDays <= 8) yield STAGE_3;
                else yield STAGE_4;
            }
            case 20 -> {
                if (completedDays <= 5) yield STAGE_1;
                else if (completedDays <= 10) yield STAGE_2;
                else if (completedDays <= 15) yield STAGE_3;
                else yield STAGE_4;
            }
            case 30 -> {
                if (completedDays <= 7) yield STAGE_1;
                else if (completedDays <= 15) yield STAGE_2;
                else if (completedDays <= 23) yield STAGE_3;
                else yield STAGE_4;
            }
            default -> STAGE_1;
        };
    }
}