package com.recordmanagement.habitlog.domain.exercise.domain.repository;

import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.util.Optional;

/**
 * 운동 기록 보안 검증 저장소 인터페이스
 * 
 * ISP 적용: 사용자별 접근 제어 기능만 분리
 * - 사용자 소유권 검증이 필요한 기능만 제공
 * - 보안이 중요한 클라이언트를 위한 인터페이스
 * - 권한 없는 접근 방지
 * 
 * 책임:
 * - 사용자별 조회 (소유권 검증)
 * - 사용자별 삭제 (소유권 검증)
 * - 존재 여부 확인 (소유권 검증)
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (ISP 적용)
 */
public interface ExerciseRecordSecurityRepository {

    /**
     * ID와 사용자 ID로 운동 기록 조회 (소유권 검증)
     * 
     * @param id 운동 기록 ID
     * @param userId 사용자 ID
     * @return 조회된 운동 기록 (소유자가 일치하지 않으면 Optional.empty())
     */
    Optional<ExerciseRecord> findByIdAndUserId(ExerciseRecordId id, UserId userId);

    /**
     * ID와 사용자 ID로 운동 기록 삭제 (소유권 검증)
     * 
     * @param id 운동 기록 ID
     * @param userId 사용자 ID
     */
    void deleteByIdAndUserId(ExerciseRecordId id, UserId userId);

    /**
     * 사용자의 운동 기록 존재 여부 확인 (소유권 검증)
     * 
     * @param id 운동 기록 ID
     * @param userId 사용자 ID
     * @return 존재 여부 (소유자가 일치하는 경우만 true)
     */
    boolean existsByIdAndUserId(ExerciseRecordId id, UserId userId);
}