package com.recordmanagement.habitlog.api.user.dto;

import com.recordmanagement.habitlog.domain.record.model.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.recordmanagement.habitlog.common.validation.ValidBirthDate;
import java.time.LocalDate;

/**
 * 온보딩 완료 요청 DTO
 * 
 * - 모든 온보딩 데이터를 한번에 전송
 * - 닉네임, 메인 기록 타입, 생년월일, 목표 일수 포함
 */
@Getter
@NoArgsConstructor
@Schema(description = "온보딩 완료 요청")
public class OnboardingCompletionRequest {
    
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 1, max = 6, message = "닉네임은 1-6글자만 가능합니다")
    @Pattern(
        regexp = "^[가-힣a-zA-Z0-9]+$", 
        message = "닉네임은 한글, 영문, 숫자만 가능하며 공백이나 특수기호는 사용할 수 없습니다"
    )
    @Schema(description = "사용자 닉네임", example = "홍길동")
    private String nickname;
    
    @NotNull(message = "메인 기록 타입은 필수입니다")
    @Schema(description = "메인 기록 타입", example = "EXERCISE")
    private RecordType mainRecordType;
    
    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    @ValidBirthDate
    @Schema(description = "생년월일", example = "1998-06-02")
    private LocalDate birthDate;
    
    @NotNull(message = "목표 일수는 필수입니다")
    @Min(value = 10, message = "목표 일수는 최소 10일이어야 합니다")
    @Max(value = 30, message = "목표 일수는 최대 30일이어야 합니다")
    @Schema(description = "목표 일수", example = "20")
    private Integer goalDays;
    
    @NotNull(message = "알림 허용 여부는 필수입니다")
    @Schema(description = "알림 허용 여부", example = "true")
    private Boolean notificationEnabled;
}