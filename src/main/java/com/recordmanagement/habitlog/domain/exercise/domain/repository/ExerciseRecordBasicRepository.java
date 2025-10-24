package com.recordmanagement.habitlog.domain.exercise.domain.repository;

import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecordId;

import java.util.Optional;

/**
 * 운동 기록 기본 저장소 인터페이스
 * 
 * ISP 적용: 기본 CRUD 기능만 분리
 * - 단순한 저장/조회/삭제 기능만 제공
 * - 클라이언트는 불필요한 복잡한 메서드에 의존하지 않음
 * 
 * 책임:
 * - 기본 저장 및 조회
 * - ID 기반 단순 삭제
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (ISP 적용)
 */
public interface ExerciseRecordBasicRepository {

    /**
     * 운동 기록 저장 또는 업데이트
     * 
     * @param exerciseRecord 저장할 운동 기록
     * @return 저장된 운동 기록
     */
    ExerciseRecord save(ExerciseRecord exerciseRecord);

    /**
     * ID로 운동 기록 조회
     * 
     * @param id 운동 기록 ID
     * @return 조회된 운동 기록 (없으면 Optional.empty())
     */
    Optional<ExerciseRecord> findById(ExerciseRecordId id);

    /**
     * ID로 운동 기록 삭제
     * 
     * @param id 삭제할 운동 기록 ID
     */
    void deleteById(ExerciseRecordId id);
}