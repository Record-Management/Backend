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
 * 하루에 2개 기록 작성 시 메인/서브 기록을 결정하는 로직을 담당합니다.
 *
 * 결정 규칙:
 * 1. 같은 종류 2개: 첫 번째를 메인으로
 * 2. 다른 종류 2개: 사용자 메인기록타입과 일치하는 걸 메인으로
 * 3. 그 외: 서브 기록으로
 *
 * @author 전우선
 * @since 2025.10.27
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainRecordDeterminationService {

    private final UserRepository userRepository;

    /**
     * 새로운 기록이 메인 기록인지 결정
     *
     * @param userId 사용자 ID
     * @param recordType 기록 타입
     * @param recordDate 기록 날짜
     * @param existingRecordCount 기존 동일 타입 기록 개수
     * @return true: 메인 기록, false: 서브 기록
     */
    public boolean determineMainRecord(UserId userId, RecordType recordType, LocalDate recordDate, int existingRecordCount) {
        log.info("메인 기록 결정 시작: userId={}, recordType={}, existingCount={}", 
                userId.getValue(), recordType, existingRecordCount);

        // 첫 번째 기록이면 메인 기록
        if (existingRecordCount == 0) {
            log.info("첫 번째 기록으로 메인 기록 결정: userId={}", userId.getValue());
            return true;
        }

        // 두 번째 기록이면 사용자의 메인 기록 타입과 비교
        if (existingRecordCount == 1) {
            RecordType userMainRecordType = getUserMainRecordType(userId);
            
            // 사용자 메인 기록 타입과 일치하면 메인으로 설정하고 기존 기록을 서브로 변경
            if (recordType == userMainRecordType) {
                log.info("사용자 메인 기록 타입과 일치하여 메인 기록으로 결정: userId={}, userMainType={}", 
                        userId.getValue(), userMainRecordType);
                return true;
            } else {
                log.info("사용자 메인 기록 타입과 불일치하여 서브 기록으로 결정: userId={}, userMainType={}, currentType={}", 
                        userId.getValue(), userMainRecordType, recordType);
                return false;
            }
        }

        // 세 번째 이상 기록은 서브 기록 (실제로는 2개 제한으로 도달 불가)
        log.info("세 번째 이상 기록으로 서브 기록 결정: userId={}", userId.getValue());
        return false;
    }

    /**
     * 기록 수정 시 메인 기록 여부 결정
     * 
     * 수정 시에는 변경하려는 기록 타입이 사용자 메인 기록 타입과 일치하는지만 확인
     *
     * @param userId 사용자 ID
     * @param newRecordType 변경하려는 기록 타입
     * @return true: 메인 기록으로 설정, false: 서브 기록으로 설정
     */
    public boolean determineMainRecordOnUpdate(UserId userId, RecordType newRecordType) {
        log.info("기록 수정 시 메인 기록 결정: userId={}, newRecordType={}", 
                userId.getValue(), newRecordType);

        RecordType userMainRecordType = getUserMainRecordType(userId);
        
        boolean isMainRecord = (newRecordType == userMainRecordType);
        
        log.info("기록 수정 시 메인 기록 결정 완료: userId={}, userMainType={}, newType={}, isMain={}", 
                userId.getValue(), userMainRecordType, newRecordType, isMainRecord);
        
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