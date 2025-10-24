package com.recordmanagement.habitlog.domain.auth.infrastructure.user.repository;

import com.recordmanagement.habitlog.domain.user.infrastructure.entity.UserEntity;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 JPA 저장소 인터페이스
 *
 * - Spring Data JPA를 활용하여 DB 접근 담당
 * - UserEntity 기준 CRUD 및 커스텀 조회 메서드 제공
 * - 소셜 로그인 관련 사용자 조회 및 존재 여부 확인 지원
 *
 * @since 1.0.0
 * @version 1.0.0
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {

    /**
     * 소셜 타입과 소셜 ID로 활성 사용자 조회 (탈퇴하지 않은 사용자만)
     *
     * @param socialType 소셜 로그인 플랫폼 타입 (KAKAO, APPLE 등)
     * @param socialId 소셜 플랫폼 내 사용자 고유 ID
     * @return Optional에 감싼 UserEntity, 없으면 빈 Optional 반환
     */
    Optional<UserEntity> findBySocialTypeAndSocialIdAndDeletedAtIsNull(SocialType socialType, String socialId);
    
    /**
     * 소셜 타입과 소셜 ID로 사용자 조회 (탈퇴 사용자 포함)
     *
     * @param socialType 소셜 로그인 플랫폼 타입 (KAKAO, APPLE 등)
     * @param socialId 소셜 플랫폼 내 사용자 고유 ID
     * @return Optional에 감싼 UserEntity, 없으면 빈 Optional 반환
     */
    Optional<UserEntity> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    /**
     * 이메일과 소셜 타입으로 활성 사용자 조회 (탈퇴하지 않은 사용자만)
     * Apple 재로그인 시 기존 사용자 찾기용
     *
     * @param email 사용자 이메일
     * @param socialType 소셜 로그인 플랫폼 타입
     * @return Optional에 감싼 UserEntity, 없으면 빈 Optional 반환
     */
    Optional<UserEntity> findByEmailAndSocialTypeAndDeletedAtIsNull(String email, SocialType socialType);
    
    /**
     * 이메일과 소셜 타입으로 사용자 조회 (탈퇴 사용자 포함)
     * Apple 재로그인 시 기존 사용자 찾기용
     *
     * @param email 사용자 이메일
     * @param socialType 소셜 로그인 플랫폼 타입
     * @return Optional에 감싼 UserEntity, 없으면 빈 Optional 반환
     */
    Optional<UserEntity> findByEmailAndSocialType(String email, SocialType socialType);

    /**
     * 소셜 타입과 소셜 ID로 사용자 존재 여부 확인
     *
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 사용자 ID
     * @return 존재하면 true, 아니면 false
     */
    boolean existsBySocialTypeAndSocialId(SocialType socialType, String socialId);

    /**
     * 닉네임 존재 여부 확인
     *
     * @param name 확인할 닉네임
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByName(String name);

    /**
     * 7일이 경과한 탈퇴 사용자 조회
     * 
     * @param deletionScheduledAt 삭제 예정 시간 기준 (현재 시간보다 이전)
     * @return 영구 삭제 대상 탈퇴 사용자 엔티티 목록
     */
    List<UserEntity> findByDeletionScheduledAtBeforeAndDeletedAtIsNotNull(LocalDateTime deletionScheduledAt);
}