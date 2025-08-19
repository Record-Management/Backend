package com.recordmanagement.habitlog.application.record;

import com.recordmanagement.habitlog.application.record.dto.DailyRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.domain.record.model.DailyRecord;
import com.recordmanagement.habitlog.domain.record.repository.DailyRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 일상 기록 애플리케이션 서비스
 * 
 * 일상 기록과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 도메인 모델과 인프라스트럭처 계층 사이의 조정 역할을 담당합니다.
 * 
 * 주요 책임:
 * - 일상 기록 생성/수정/삭제 비즈니스 로직 처리
 * - 도메인 규칙 적용 (하루 하나 기록 제한 등)
 * - 트랜잭션 관리
 * - 도메인 객체와 DTO 간 변환
 * - 로깅 및 예외 처리
 * 
 * 트랜잭션 정책:
 * - 모든 쓰기 작업은 @Transactional 적용
 * - 읽기 전용 작업은 @Transactional(readOnly = true) 적용
 * - 런타임 예외 발생 시 자동 롤백
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DailyRecordApplicationService {

    private final DailyRecordRepository dailyRecordRepository;

    /**
     * 일상 기록 생성 또는 수정
     * 
     * 사용자의 일상 기록을 생성하거나 기존 기록을 수정하는 핵심 비즈니스 로직입니다.
     * 하루에 하나의 일상 기록만 허용하는 제약 조건을 관리합니다.
     * 
     * 처리 흐름:
     * 1. 사용자 ID와 기록 날짜로 기존 기록 조회
     * 2. 기존 기록 존재 시:
     *    - 기존 기록의 내용을 새로운 데이터로 수정
     *    - updatedAt 필드 자동 갱신
     *    - 수정된 기록을 저장소에 저장
     * 3. 기존 기록 미존재 시:
     *    - 새로운 일상 기록 도메인 객체 생성
     *    - 고유 ID와 생성/수정 시간 자동 설정
     *    - 새 기록을 저장소에 저장
     * 4. 저장된 기록을 응답 DTO로 변환하여 반환
     * 
     * 비즈니스 규칙:
     * - 사용자당 하루에 하나의 일상 기록만 허용
     * - 기분(mood) 필드는 필수 입력
     * - 제목, 내용, 이미지 URL은 선택 입력 (null 허용)
     * - 기록 날짜는 변경 불가 (수정 시에도 동일 날짜 유지)
     * 
     * @param command 일상 기록 생성/수정에 필요한 데이터를 담은 커맨드 객체
     * @return DailyRecordResponse 생성/수정된 일상 기록 정보
     * 
     * @throws IllegalArgumentException command의 필수 필드가 null인 경우
     * @throws DataAccessException 데이터베이스 접근 중 오류 발생 시
     */
    public DailyRecordResponse createOrUpdateDailyRecord(DailyRecordCreateCommand command) {
        log.info("일상 기록 생성/수정: userId={}, date={}", command.getUserId(), command.getRecordDate());
        
        UserId userId = UserId.of(command.getUserId());
        
        // 기존 기록이 있는지 확인
        Optional<DailyRecord> existingRecord = dailyRecordRepository.findByUserIdAndRecordDate(
                userId, command.getRecordDate());
        
        DailyRecord dailyRecord;
        
        if (existingRecord.isPresent()) {
            // 기존 기록 수정
            dailyRecord = existingRecord.get();
            dailyRecord.updateRecord(
                    command.getMood(),
                    command.getTitle(),
                    command.getContent(),
                    command.getImageUrl()
            );
            log.info("일상 기록 수정됨: recordId={}", dailyRecord.getId());
        } else {
            // 새 기록 생성
            dailyRecord = new DailyRecord(
                    userId,
                    command.getRecordDate(),
                    command.getMood(),
                    command.getTitle(),
                    command.getContent(),
                    command.getImageUrl()
            );
            log.info("새 일상 기록 생성됨: recordId={}", dailyRecord.getId());
        }
        
        DailyRecord savedRecord = dailyRecordRepository.save(dailyRecord);
        
        return DailyRecordResponse.from(savedRecord);
    }

    /**
     * 일상 기록 삭제
     */
    public void deleteDailyRecord(String userId, LocalDate recordDate) {
        log.info("일상 기록 삭제: userId={}, date={}", userId, recordDate);
        
        UserId userIdObj = UserId.of(userId);
        
        Optional<DailyRecord> dailyRecord = dailyRecordRepository.findByUserIdAndRecordDate(
                userIdObj, recordDate);
        
        if (dailyRecord.isPresent()) {
            dailyRecordRepository.delete(dailyRecord.get());
            log.info("일상 기록 삭제됨: recordId={}", dailyRecord.get().getId());
        } else {
            log.warn("삭제할 일상 기록이 없습니다: userId={}, date={}", userId, recordDate);
        }
    }
}