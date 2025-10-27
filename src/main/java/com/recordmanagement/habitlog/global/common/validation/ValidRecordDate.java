package com.recordmanagement.habitlog.global.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 기록 날짜 유효성 검증 어노테이션
 *
 * - 기록 날짜가 유효한 형식인지 검증
 * - 오늘 날짜만 허용 (과거/미래 날짜 모두 불가)
 *
 * @author 전우선
 * @since 2025.10.27
 * @version 1.0.0
 */
@Documented
@Constraint(validatedBy = ValidRecordDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRecordDate {
    
    String message() default "올바르지 않은 기록 날짜입니다.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}