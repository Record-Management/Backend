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
 * 실행 시점: 매일 자정 (00:00:00) - 한국시간 기준
 *
 * @author 전우선
 * @since 2026.03.07
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HabitRecordAutoGenerationScheduler {

    private final UserRepository userRepository;
    private final HabitRecordRepository habitRecordRepository;

    /**
     * 매일 자정에 습관 타입 사용자의 오늘 날짜 메인 습관 기록 자동 생성
     * 한국시간(KST) 기준으로 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
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

            log.info("습관 타입 사용자 {}명 발견. 자동 생성 시작", habitTypeUsers.size());

            int generatedCount = 0;
            int skippedCount = 0;

            for (User user : habitTypeUsers) {
                try {
                    // 2. 습관 시작일이 설정되어 있는지 확인
                    if (user.getHabitStartDate() == null || user.getGoalDays() == null) {
                        log.debug("습관 시작일 미설정 - userId={}", user.getId().getValue());
                        skippedCount++;
                        continue;
                    }

                    // 3. 오늘이 습관 기간 내인지 확인
                    if (!user.isWithinHabitPeriod(today)) {
                        log.debug("습관 기간 외 - userId={}, today={}, habitPeriod=[{} ~ {}]",
                                user.getId().getValue(), today,
                                user.getHabitStartDate(),
                                user.getHabitStartDate().plusDays(user.getGoalDays() - 1));
                        skippedCount++;
                        continue;
                    }

                    // 4. 오늘 날짜에 이미 메인 습관 기록이 있는지 확인
                    boolean hasMainHabitRecord = habitRecordRepository.existsMainRecordByUserIdAndRecordDate(
                            user.getId(), today
                    );

                    if (hasMainHabitRecord) {
                        log.debug("오늘 메인 습관 기록 이미 존재 - userId={}, date={}",
                                user.getId().getValue(), today);
                        skippedCount++;
                        continue;
                    }

                    // 5. 메인 습관 기록 자동 생성 (placeholder)
                    // 사용자가 메인 습관을 작성한 적이 있는지 확인 (habitType 결정용)
                    List<HabitRecord> userHabitRecords = habitRecordRepository.findByUserIdAndRecordDate(
                            user.getId(), today.minusDays(1)  // 어제 기록 참조
                    );

                    HabitRecord placeholderRecord;
                    if (!userHabitRecords.isEmpty() && userHabitRecords.get(0).isMainRecord()) {
                        // 어제 메인 습관 기록이 있으면 동일한 habitType으로 생성
                        HabitRecord previousMainRecord = userHabitRecords.stream()
                                .filter(HabitRecord::isMainRecord)
                                .findFirst()
                                .orElse(userHabitRecords.get(0));

                        placeholderRecord = HabitRecord.create(
                                user.getId(),
                                previousMainRecord.getHabitType(),
                                false,  // 알림 비활성화
                                null,   // 알림 시간 없음
                                null,   // 메모 없음
                                today
                        );
                    } else {
                        // 첫 습관 기록이거나 이전 기록 없으면 기본값으로 생성
                        placeholderRecord = HabitRecord.create(
                                user.getId(),
                                com.recordmanagement.habitlog.domain.habit.domain.model.HabitType.WATER_DRINKING,
                                false,
                                null,
                                null,
                                today
                        );
                    }

                    // 메인 기록으로 설정
                    placeholderRecord = placeholderRecord.updateMainRecordStatus(true);

                    // 저장
                    habitRecordRepository.save(placeholderRecord);
                    generatedCount++;

                    log.info("메인 습관 기록 자동 생성 완료 - userId={}, recordDate={}, habitType={}",
                            user.getId().getValue(), today, placeholderRecord.getHabitType());

                } catch (Exception e) {
                    log.error("사용자 습관 기록 자동 생성 실패 - userId={}, error={}",
                            user.getId().getValue(), e.getMessage(), e);
                    // 개별 사용자 실패가 전체 스케줄러를 중단시키지 않도록 함
                }
            }

            log.info("=== 습관 기록 자동 생성 완료 - 생성: {}, 건너뜀: {} ===", generatedCount, skippedCount);

        } catch (Exception e) {
            log.error("습관 기록 자동 생성 스케줄러 실행 중 오류 발생", e);
        }
    }
}
