package com.recordmanagement.habitlog.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExerciseDataValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExerciseData {
    String message() default "운동기록 중 최소 1개 항목(칼로리, 운동시간, 걸음수)은 필수입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}