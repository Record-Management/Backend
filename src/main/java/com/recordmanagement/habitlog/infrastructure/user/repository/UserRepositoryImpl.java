package com.recordmanagement.habitlog.infrastructure.user.repository;

import com.recordmanagement.habitlog.domain.user.model.User;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.domain.user.repository.UserRepository;
import com.recordmanagement.habitlog.infrastructure.user.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
     * 소셜 타입 및 소셜 ID로 사용자 조회
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
}