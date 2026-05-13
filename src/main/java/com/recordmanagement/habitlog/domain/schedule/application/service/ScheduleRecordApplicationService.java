package com.recordmanagement.habitlog.domain.schedule.application.service;

import com.recordmanagement.habitlog.domain.schedule.application.dto.CreateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.application.dto.ScheduleResponse;
import com.recordmanagement.habitlog.domain.schedule.application.dto.UpdateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecordId;
import com.recordmanagement.habitlog.domain.schedule.domain.repository.ScheduleRecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleRecordApplicationService {

    private final ScheduleRecordRepository scheduleRecordRepository;

    /**
     * 일정 생성
     */
    public ScheduleResponse create(String userId, CreateScheduleCommand command) {
        log.info("Creating schedule for user: {}", userId);

        // 입력 검증
        validateScheduleCommand(command.getStartDate(), command.getEndDate(),
                              command.getNotificationType(), command.getNotificationCustomDays(),
                              command.getNotificationCustomHours());

        ScheduleRecord scheduleRecord = ScheduleRecord.create(
            UserId.of(userId),
            command.getTitle(),
            command.getStartDate(),
            command.getEndDate(),
            command.getNotificationType(),
            command.getNotificationCustomDays(),
            command.getNotificationCustomHours(),
            command.getRepeatType(),
            command.getRepeatEndsOn(),
            command.getLocation(),
            command.getColor(),
            command.getMemo()
        );

        ScheduleRecord savedSchedule = scheduleRecordRepository.save(scheduleRecord);
        return ScheduleResponse.from(savedSchedule);
    }

    /**
     * 일정 수정
     */
    public ScheduleResponse update(String userId, String scheduleRecordId, UpdateScheduleCommand command) {
        log.info("Updating schedule: {} for user: {}", scheduleRecordId, userId);

        // 일정 조회 및 권한 확인
        ScheduleRecord existingSchedule = scheduleRecordRepository
            .findByIdAndUserId(ScheduleRecordId.from(scheduleRecordId), UserId.of(userId))
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 입력 검증
        validateScheduleCommand(command.getStartDate(), command.getEndDate(),
                              command.getNotificationType(), command.getNotificationCustomDays(),
                              command.getNotificationCustomHours());

        ScheduleRecord updatedSchedule = existingSchedule.update(
            command.getTitle(),
            command.getStartDate(),
            command.getEndDate(),
            command.getNotificationType(),
            command.getNotificationCustomDays(),
            command.getNotificationCustomHours(),
            command.getRepeatType(),
            command.getRepeatEndsOn(),
            command.getLocation(),
            command.getColor(),
            command.getMemo()
        );

        ScheduleRecord savedSchedule = scheduleRecordRepository.save(updatedSchedule);
        return ScheduleResponse.from(savedSchedule);
    }

    /**
     * 일정 삭제
     */
    public void delete(String userId, String scheduleRecordId) {
        log.info("Deleting schedule: {} for user: {}", scheduleRecordId, userId);

        // 일정 존재 및 권한 확인
        if (!scheduleRecordRepository.existsByIdAndUserId(
                ScheduleRecordId.from(scheduleRecordId), UserId.of(userId))) {
            throw new CustomException(ErrorCode.RECORD_NOT_FOUND);
        }

        scheduleRecordRepository.deleteByIdAndUserId(
            ScheduleRecordId.from(scheduleRecordId), UserId.of(userId)
        );
    }

    /**
     * 일정 단건 조회
     */
    @Transactional(readOnly = true)
    public ScheduleResponse findById(String userId, String scheduleRecordId) {
        log.info("Finding schedule: {} for user: {}", scheduleRecordId, userId);

        ScheduleRecord scheduleRecord = scheduleRecordRepository
            .findByIdAndUserId(ScheduleRecordId.from(scheduleRecordId), UserId.of(userId))
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        return ScheduleResponse.from(scheduleRecord);
    }

    /**
     * 날짜 범위별 일정 조회 (캘린더용)
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> findByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        log.info("Finding schedules for user: {} between {} and {}", userId, startDate, endDate);

        List<ScheduleRecord> schedules = scheduleRecordRepository
            .findByUserIdAndDateRange(UserId.of(userId), startDate, endDate);

        return schedules.stream()
            .map(ScheduleResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 일정 입력 검증
     */
    private void validateScheduleCommand(LocalDate startDate, LocalDate endDate,
                                        com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType notificationType,
                                        Integer customDays, Integer customHours) {
        // 시작일이 종료일보다 늦으면 안 됨
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // CUSTOM 알림일 경우 customDays와 customHours 필수
        if (notificationType == com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType.CUSTOM) {
            if (customDays == null || customHours == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (customDays < 0 || customHours < 0 || customHours > 23) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }
}
