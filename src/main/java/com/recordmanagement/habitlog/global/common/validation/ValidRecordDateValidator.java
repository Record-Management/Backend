package com.recordmanagement.habitlog.global.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 기록 날짜 유효성 검증기
 *
 * - 날짜 형식 검증 (yyyy-MM-dd)
 * - 오늘 날짜만 허용 (과거/미래 날짜 모두 불가)
 *
 * @author 전우선
 * @since 2025.10.27
 * @version 1.0.0
 */
@Slf4j
public class ValidRecordDateValidator implements ConstraintValidator<ValidRecordDate, String> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public void initialize(ValidRecordDate constraintAnnotation) {
        // 초기화 로직 (필요 시 추가)
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 또는 빈 문자열은 다른 어노테이션(@NotNull, @NotBlank)에서 처리
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        try {
            // 1. 날짜 형식 검증
            LocalDate recordDate = LocalDate.parse(value, DATE_FORMAT);
            LocalDate today = LocalDate.now();
            
            // 2. 오늘만 허용 (과거/미래 날짜 모두 불가)
            if (!recordDate.equals(today)) {
                if (recordDate.isAfter(today)) {
                    setCustomMessage(context, "기록 날짜는 미래 날짜일 수 없습니다.");
                } else {
                    setCustomMessage(context, "기록 날짜는 오늘 날짜만 허용됩니다.");
                }
                return false;
            }
            
            return true;
            
        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식: {}", value);
            setCustomMessage(context, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd 형식으로 입력해주세요)");
            return false;
        }
    }
    
    /**
     * 커스텀 에러 메시지 설정
     */
    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}