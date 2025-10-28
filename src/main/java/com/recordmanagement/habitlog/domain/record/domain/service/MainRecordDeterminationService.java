package com.recordmanagement.habitlog.domain.record.domain.service;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 메인 기록 결정 서비스
 *
 * 기록 작성/수정 시 메인/서브 기록을 결정하는 로직을 담당합니다.
 *
 * 결정 규칙:
 * - 사용자의 메인 기록 타입과 일치하면 → 메인 기록
 * - 사용자의 메인 기록 타입과 불일치하면 → 서브 기록
 * - 작성 순서, 기록 개수와는 무관
 *
 * @author 전우선
 * @since 2025.10.27
 * @version 2.0.0 (정책 단순화)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainRecordDeterminationService {

    private final UserRepository userRepository;

    /**
     * 새로운 기록이 메인 기록인지 결정
     * 
     * 사용자의 메인 기록 타입과 일치 여부만 확인
     *
     * @param userId 사용자 ID
     * @param recordType 기록 타입
     * @param recordDate 기록 날짜 (현재 사용 안함, 호환성 유지)
     * @param existingRecordCount 기존 동일 타입 기록 개수 (현재 사용 안함, 호환성 유지)
     * @return true: 메인 기록, false: 서브 기록
     */
    public boolean determineMainRecord(UserId userId, RecordType recordType, LocalDate recordDate, int existingRecordCount) {
        log.info("메인 기록 결정 시작: userId={}, recordType={}", 
                userId.getValue(), recordType);

        RecordType userMainRecordType = getUserMainRecordType(userId);
        boolean isMainRecord = (recordType == userMainRecordType);
        
        log.info("메인 기록 결정 완료: userId={}, userMainType={}, recordType={}, isMain={}", 
                userId.getValue(), userMainRecordType, recordType, isMainRecord);
        
        return isMainRecord;
    }

    /**
     * 기록 수정 시 메인 기록 여부 결정
     * 
     * 사용자의 메인 기록 타입과 일치 여부만 확인 (생성 시와 동일한 로직)
     *
     * @param userId 사용자 ID
     * @param recordType 기록 타입
     * @return true: 메인 기록으로 설정, false: 서브 기록으로 설정
     */
    public boolean determineMainRecordOnUpdate(UserId userId, RecordType recordType) {
        log.info("기록 수정 시 메인 기록 결정: userId={}, recordType={}", 
                userId.getValue(), recordType);

        RecordType userMainRecordType = getUserMainRecordType(userId);
        boolean isMainRecord = (recordType == userMainRecordType);
        
        log.info("기록 수정 시 메인 기록 결정 완료: userId={}, userMainType={}, recordType={}, isMain={}", 
                userId.getValue(), userMainRecordType, recordType, isMainRecord);
        
        return isMainRecord;
    }

    /**
     * 사용자의 메인 기록 타입 조회
     *
     * @param userId 사용자 ID
     * @return 사용자의 메인 기록 타입
     */
    private RecordType getUserMainRecordType(UserId userId) {
        return userRepository.findById(userId)
                .map(user -> user.getMainRecordType())
                .orElse(RecordType.DAILY); // 기본값은 일상 기록
    }
}