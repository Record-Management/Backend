package com.recordmanagement.habitlog.domain.user.service;

import com.recordmanagement.habitlog.domain.user.model.User;
import com.recordmanagement.habitlog.domain.user.model.Email;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 사용자 도메인 서비스
 *
 * 역할:
 * - 단순 저장소 접근 이상의 도메인 규칙이 필요한 경우 수행
 * - 복잡한 도메인 생성 로직 캡슐화
 * - 도메인 모델 간 협력을 중재
 *
 * 왜 필요한가?
 * - 도메인 모델이 표현하기 어려운 비즈니스 규칙 분리
 * - 사용자 생성 전 중복 검증 같은 복합 로직 담당
 *
 * 일반적으로 Application Service(UseCase)와 구분됨
 * - Domain Service: 도메인 레이어에 위치, 도메인 규칙 구현
 * - Application Service: 유스케이스(흐름 제어) 중심
 *
 * 예외 처리:
 * - 중복 사용자가 있는 경우 IllegalArgumentException 발생
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    /**
     * 사용자 중복 여부 확인
     *
     * @param socialType 소셜 로그인 플랫폼 (예: KAKAO, GOOGLE)
     * @param socialId 해당 플랫폼의 사용자 고유 식별자
     * @return true: 이미 존재하는 사용자, false: 신규 사용자
     */
    public boolean isDuplicateUser(SocialType socialType, String socialId) {
        return userRepository.existsBySocialTypeAndSocialId(socialType, socialId);
    }

    /**
     * 새로운 사용자 생성 (중복 검증 포함)
     *
     * @param name 사용자 이름
     * @param email 사용자 이메일 주소 (형식 검증 포함됨)
     * @param socialType 로그인 플랫폼 종류
     * @param socialId 소셜 플랫폼 사용자 ID
     * @return 생성된 사용자 도메인 객체
     * @throws IllegalArgumentException 중복 사용자 존재 시
     */
    public User createNewUser(String name, String email, SocialType socialType, String socialId) {
        if (isDuplicateUser(socialType, socialId)) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        return new User(name, Email.of(email), socialType, socialId);
    }
}

