package com.recordmanagement.habitlog.domain.user.domain.event;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 탈퇴 도메인 이벤트
 *
 * - 사용자 탈퇴 시 발생하는 도메인 이벤트
 * - 알림 설정, 알림 히스토리 등 관련 데이터 정리를 위한 이벤트
 * - 느슨한 결합을 통한 사이드 이펙트 처리
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class UserWithdrawnEvent {

    private final UserId userId;
    private final String reason;
}