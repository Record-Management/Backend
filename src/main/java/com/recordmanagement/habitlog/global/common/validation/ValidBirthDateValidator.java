package com.recordmanagement.habitlog.global.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

/**
 * ValidBirthDate 어노테이션의 실제 검증 로직을 담은 Validator
 */
public class ValidBirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {

    private static final LocalDate MIN_BIRTH_DATE = LocalDate.of(1900, 1, 1);
    private static final int MAX_AGE = 150;
    private static final int MIN_AGE = 3;

    @Override
    public void initialize(ValidBirthDate constraintAnnotation) {
        // 초기화 로직 (필요시)
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true; // null 검증은 @NotNull에서 처리
        }

        LocalDate today = LocalDate.now();
        
        // 1. 1900년 이후 날짜인지 검증
        if (birthDate.isBefore(MIN_BIRTH_DATE)) {
            setCustomMessage(context, "생년월일은 1900년 1월 1일 이후여야 합니다.");
            return false;
        }
        
        // 2. 미래 날짜가 아닌지 검증
        if (birthDate.isAfter(today)) {
            setCustomMessage(context, "생년월일은 미래 날짜일 수 없습니다.");
            return false;
        }
        
        // 3. 나이 계산 및 검증
        Period period = Period.between(birthDate, today);
        int age = period.getYears();
        
        // 4. 최대 나이 검증 (150세 초과 불가)
        if (age > MAX_AGE) {
            setCustomMessage(context, "나이가 " + MAX_AGE + "세를 초과할 수 없습니다.");
            return false;
        }
        
        // 5. 최소 나이 검증 (3세 미만 불가)
        if (age < MIN_AGE) {
            setCustomMessage(context, "최소 " + MIN_AGE + "세 이상이어야 합니다.");
            return false;
        }

        return true;
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