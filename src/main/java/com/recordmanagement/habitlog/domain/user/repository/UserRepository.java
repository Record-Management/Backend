package com.recordmanagement.habitlog.domain.user.repository;

import com.recordmanagement.habitlog.domain.user.model.User;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

/**
 * 사용자 도메인 저장소 인터페이스
 *
 * - 도메인 레이어에서 사용자 관련 데이터 저장, 조회, 삭제 기능을 추상화
 * - 구체적인 DB 접근은 Infrastructure 레이어에서 구현
 *
 * 주요 기능:
 * - 사용자 저장 및 조회 (ID, 소셜 타입+소셜 ID 기반)
 * - 사용자 존재 여부 확인
 * - 사용자 삭제
 *
 * 설계 원칙:
 * - 도메인 순수성을 위해 비즈니스 로직은 포함하지 않음
 * - Optional로 null 처리 방지
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Schema(description = "사용자 도메인 저장소 인터페이스")
public interface UserRepository {

    /**
     * 사용자 엔터티 저장
     *
     * @param user 저장할 사용자 엔터티
     * @return 저장된 사용자 엔터티 (ID 포함)
     */
    User save(User user);

    /**
     * 사용자 ID로 조회
     *
     * @param id 사용자 고유 ID
     * @return 조회된 사용자 (없으면 Optional.empty())
     */
    Optional<User> findById(UserId id);

    /**
     * 소셜 로그인 타입과 소셜 ID로 사용자 조회
     *
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 사용자 고유 ID
     * @return 조회된 사용자 (없으면 Optional.empty())
     */
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    /**
     * 이메일과 소셜 타입으로 사용자 조회
     * Apple 재로그인 시 기존 사용자 찾기용
     *
     * @param email 사용자 이메일
     * @param socialType 소셜 로그인 플랫폼 타입
     * @return 조회된 사용자 (없으면 Optional.empty())
     */
    Optional<User> findByEmailAndSocialType(String email, SocialType socialType);

    /**
     * 사용자 엔터티 삭제
     *
     * @param user 삭제할 사용자 엔터티
     */
    void delete(User user);

    /**
     * 소셜 로그인 타입과 소셜 ID 존재 여부 확인
     *
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 사용자 고유 ID
     * @return true: 존재함, false: 존재하지 않음
     */
    boolean existsBySocialTypeAndSocialId(SocialType socialType, String socialId);

    /**
     * 닉네임(name) 존재 여부 확인
     *
     * @param name 확인할 닉네임
     * @return true: 존재함, false: 존재하지 않음
     */
    boolean existsByName(String name);
}