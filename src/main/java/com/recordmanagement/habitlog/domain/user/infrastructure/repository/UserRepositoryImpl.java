package com.recordmanagement.habitlog.domain.auth.infrastructure.user.repository;

import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.infrastructure.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User Repository 구현체
 *
 * - 도메인 레이어에서 정의된 UserRepository 인터페이스의 인프라스트럭처 구현
 * - User 엔터티와 UserEntity 간 변환을 처리하며
 *   UserJpaRepository를 통해 DB 작업 수행
 * - 도메인 모델에 맞춘 데이터 변환 책임을 가짐
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * 사용자 저장 (신규 생성 및 수정 모두 처리)
     *
     * @param user 도메인 User 객체
     * @return 저장된 User 도메인 객체 (DB 반영 결과)
     */
    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.from(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    /**
     * 사용자 ID로 조회
     *
     * @param id UserId 값 객체
     * @return Optional<User> 조회 결과 (없으면 빈 Optional)
     */
    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.getValue())
                .map(UserEntity::toDomain);
    }

    /**
     * 소셜 타입 및 소셜 ID로 사용자 조회 (탈퇴 사용자 포함)
     *
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 내 사용자 고유 ID
     * @return Optional<User> 조회 결과
     */
    @Override
    public Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId) {
        return userJpaRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .map(UserEntity::toDomain);
    }

    /**
     * 이메일과 소셜 타입으로 사용자 조회 (탈퇴 사용자 포함)
     * Apple 재로그인 시 기존 사용자 찾기용
     *
     * @param email 사용자 이메일
     * @param socialType 소셜 로그인 플랫폼 타입
     * @return Optional<User> 조회 결과
     */
    @Override
    public Optional<User> findByEmailAndSocialType(String email, SocialType socialType) {
        return userJpaRepository.findByEmailAndSocialType(email, socialType)
                .map(UserEntity::toDomain);
    }

    /**
     * 사용자 삭제
     *
     * @param user 삭제할 User 도메인 객체
     */
    @Override
    public void delete(User user) {
        UserEntity entity = UserEntity.from(user);
        userJpaRepository.delete(entity);
    }

    /**
     * 소셜 타입과 소셜 ID로 사용자 존재 여부 확인
     *
     * @param socialType 소셜 플랫폼 타입
     * @param socialId 소셜 플랫폼 사용자 ID
     * @return 존재하면 true, 아니면 false
     */
    @Override
    public boolean existsBySocialTypeAndSocialId(SocialType socialType, String socialId) {
        return userJpaRepository.existsBySocialTypeAndSocialId(socialType, socialId);
    }

    /**
     * 닉네임 존재 여부 확인
     *
     * @param name 확인할 닉네임
     * @return 존재하면 true, 아니면 false
     */
    @Override
    public boolean existsByName(String name) {
        return userJpaRepository.existsByName(name);
    }

    /**
     * 7일이 경과한 탈퇴 사용자 조회
     * 
     * @param currentTime 현재 시간
     * @return 영구 삭제 대상 탈퇴 사용자 목록
     */
    @Override
    public List<User> findExpiredWithdrawnUsers(LocalDateTime currentTime) {
        return userJpaRepository.findByDeletionScheduledAtBeforeAndDeletedAtIsNotNull(currentTime)
                .stream()
                .map(UserEntity::toDomain)
                .collect(Collectors.toList());
    }
}