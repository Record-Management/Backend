package com.recordmanagement.habitlog.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 생년월일 유효성 검증 어노테이션
 * 
 * - 과거 날짜여야 함
 * - 1900년 이후 날짜여야 함
 * - 150세를 넘을 수 없음
 * - 오늘로부터 3세 이상이어야 함 (유아 사용자 제외)
 */
@Documented
@Constraint(validatedBy = ValidBirthDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {
    
    String message() default "유효하지 않은 생년월일입니다. (1900년 이후 ~ 최대 150세까지 가능)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}