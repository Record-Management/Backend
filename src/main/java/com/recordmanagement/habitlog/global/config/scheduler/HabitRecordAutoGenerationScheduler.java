package com.recordmanagement.habitlog.global.config.scheduler;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 습관 기록 자동 생성 스케줄러
 *
 * 매일 자정에 실행되어 습관 타입 사용자의 오늘 날짜 메인 습관 기록을 자동 생성합니다.
 *
 * 처리 과정:
 * 1. mainRecordType = HABIT인 모든 사용자 조회
 * 2. 각 사용자의 습관 기간 내인지 확인 (habitStartDate ~ habitStartDate + goalDays)
 * 3. 오늘 날짜에 메인 습관 기록이 없으면 자동 생성
 *
 * 최적화:
 * - 배치 처리: 100명씩 나눠서 처리
 * - 배치 INSERT: 배치 단위로 모아서 저장
 * - 개별 트랜잭션: 배치 단위로 트랜잭션 분리
 *
 * 실행 시점: 매일 자정 (00:00:00) - 한국시간 기준
 *
 * @author 전우선
 * @since 2026.03.07
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HabitRecordAutoGenerationScheduler {

    private final UserRepository userRepository;
    private final HabitRecordRepository habitRecordRepository;

    private static final int BATCH_SIZE = 100;

    /**
     * 매일 자정에 습관 타입 사용자의 오늘 날짜 메인 습관 기록 자동 생성
     * 한국시간(KST) 기준으로 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void generateDailyMainHabitRecords() {
        log.info("=== 습관 기록 자동 생성 스케줄러 시작 - 자정 실행 ===");

        LocalDate today = LocalDate.now();

        try {
            // 1. 습관 타입 사용자 전체 조회
            List<User> habitTypeUsers = userRepository.findByMainRecordType(RecordType.HABIT);

            if (habitTypeUsers.isEmpty()) {
                log.info("습관 타입 사용자가 없습니다. 스케줄러 종료");
                return;
            }

            log.info("습관 타입 사용자 {}명 발견. 배치 처리 시작 (배치 크기: {})",
                    habitTypeUsers.size(), BATCH_SIZE);

            int totalGenerated = 0;
            int totalSkipped = 0;

            // 2. 배치 단위로 처리
            for (int i = 0; i < habitTypeUsers.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, habitTypeUsers.size());
                List<User> batch = habitTypeUsers.subList(i, endIndex);

                BatchResult result = processBatch(batch, today);
                totalGenerated += result.generated;
                totalSkipped += result.skipped;

                log.info("배치 처리 완료: [{}-{}] / {}, 생성: {}, 건너뜀: {}",
                        i + 1, endIndex, habitTypeUsers.size(), result.generated, result.skipped);
            }

            log.info("=== 습관 기록 자동 생성 완료 - 총 생성: {}, 총 건너뜀: {} ===",
                    totalGenerated, totalSkipped);

        } catch (Exception e) {
            log.error("습관 기록 자동 생성 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 배치 단위로 습관 기록 생성 처리
     * 개별 트랜잭션으로 실행되어 배치 간 영향 최소화
     */
    @Transactional
    public BatchResult processBatch(List<User> users, LocalDate today) {
        int generatedCount = 0;
        int skippedCount = 0;
        List<HabitRecord> recordsToSave = new ArrayList<>();

        for (User user : users) {
            try {
                // 습관 시작일이 설정되어 있는지 확인
                if (user.getHabitStartDate() == null || user.getGoalDays() == null) {
                    log.debug("습관 시작일 미설정 - userId={}", user.getId().getValue());
                    skippedCount++;
                    continue;
                }

                // 오늘이 습관 기간 내인지 확인
                if (!user.isWithinHabitPeriod(today)) {
                    log.debug("습관 기간 외 - userId={}, today={}, habitPeriod=[{} ~ {}]",
                            user.getId().getValue(), today,
                            user.getHabitStartDate(),
                            user.getHabitStartDate().plusDays(user.getGoalDays() - 1));
                    skippedCount++;
                    continue;
                }

                // 오늘 날짜에 이미 메인 습관 기록이 있는지 확인
                boolean hasMainHabitRecord = habitRecordRepository.existsMainRecordByUserIdAndRecordDate(
                        user.getId(), today
                );

                if (hasMainHabitRecord) {
                    log.debug("오늘 메인 습관 기록 이미 존재 - userId={}, date={}",
                            user.getId().getValue(), today);
                    skippedCount++;
                    continue;
                }

                // 메인 습관 기록 생성 (어제 기록 참조)
                HabitRecord newRecord = createMainHabitRecord(user, today);
                recordsToSave.add(newRecord);
                generatedCount++;

                log.debug("메인 습관 기록 생성 대기열 추가 - userId={}, recordDate={}, habitType={}",
                        user.getId().getValue(), today, newRecord.getHabitType());

            } catch (Exception e) {
                log.error("사용자 습관 기록 자동 생성 실패 - userId={}, error={}",
                        user.getId().getValue(), e.getMessage(), e);
                skippedCount++;
            }
        }

        // 배치 INSERT
        if (!recordsToSave.isEmpty()) {
            for (HabitRecord record : recordsToSave) {
                habitRecordRepository.save(record);
            }
            log.info("배치 INSERT 완료: {} 건", recordsToSave.size());
        }

        return new BatchResult(generatedCount, skippedCount);
    }

    /**
     * 메인 습관 기록 생성
     * 어제 메인 기록이 있으면 동일한 habitType으로, 없으면 WATER_DRINKING으로 생성
     * memo는 null로 설정하여 캘린더에 표시되도록 함
     */
    private HabitRecord createMainHabitRecord(User user, LocalDate today) {
        // 어제 메인 습관 기록 조회
        List<HabitRecord> yesterdayRecords = habitRecordRepository.findByUserIdAndRecordDate(
                user.getId(), today.minusDays(1)
        );

        HabitRecord record;
        if (!yesterdayRecords.isEmpty()) {
            // 어제 메인 습관 기록이 있으면 동일한 habitType으로 생성
            HabitRecord previousMainRecord = yesterdayRecords.stream()
                    .filter(HabitRecord::isMainRecord)
                    .findFirst()
                    .orElse(yesterdayRecords.get(0));

            record = HabitRecord.create(
                    user.getId(),
                    previousMainRecord.getHabitType(),
                    false,  // 알림 비활성화
                    null,   // 알림 시간 없음
                    null,   // 메모 없음 (캘린더에 표시되도록)
                    today
            );
        } else {
            // 첫 습관 기록이거나 이전 기록 없으면 WATER_DRINKING으로 생성
            record = HabitRecord.create(
                    user.getId(),
                    com.recordmanagement.habitlog.domain.habit.domain.model.HabitType.WATER_DRINKING,
                    false,
                    null,
                    null,  // 메모 없음 (캘린더에 표시되도록)
                    today
            );
        }

        // 메인 기록으로 설정
        return record.updateMainRecordStatus(true);
    }

    /**
     * 배치 처리 결과
     */
    private static class BatchResult {
        final int generated;
        final int skipped;

        BatchResult(int generated, int skipped) {
            this.generated = generated;
            this.skipped = skipped;
        }
    }
}
