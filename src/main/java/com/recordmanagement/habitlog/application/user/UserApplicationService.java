package com.recordmanagement.habitlog.application.user;

import com.recordmanagement.habitlog.application.user.dto.UserRegistrationCommand;
import com.recordmanagement.habitlog.application.user.dto.UserResponse;
import com.recordmanagement.habitlog.application.user.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.auth.repository.RefreshTokenRepository;
import com.recordmanagement.habitlog.domain.auth.service.SocialUnlinkService;
import com.recordmanagement.habitlog.domain.user.model.User;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.domain.user.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.service.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 애플리케이션 서비스
 * 사용자 관련 Use Case 구현 및 도메인 서비스 조합
 * 사용자 등록, 소셜 로그인 사용자 조회, ID 기반 사용자 조회, 회원탈퇴 처리
 * 트랜잭션 경계 설정 및 데이터 일관성 보장
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final SocialUnlinkService socialUnlinkService;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserApplicationService(UserRepository userRepository, 
                                UserDomainService userDomainService,
                                SocialUnlinkService socialUnlinkService,
                                RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.socialUnlinkService = socialUnlinkService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 신규 사용자 등록 처리
     * 도메인 서비스에 위임하여 User 엔터티 생성 후 저장
     * 저장된 도메인 모델을 UserResponse DTO로 변환하여 반환
     *
     * @param command 사용자 등록 커맨드 객체
     * @return 등록 완료된 사용자 정보 DTO
     */
    public UserResponse registerUser(UserRegistrationCommand command) {
        User user = userDomainService.createNewUser(
                command.getName(),
                command.getEmail(),
                command.getSocialType(),
                command.getSocialId()
        );

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    /**
     * 소셜 로그인 사용자 조회
     * 소셜 타입과 소셜 ID 조합으로 사용자 검색
     * 조회 결과를 UserResponse DTO로 변환하여 반환
     * 읽기 전용 트랜잭션 적용
     *
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 고유 사용자 ID
     * @return 사용자 정보 Optional
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findBySocialLogin(SocialType socialType, String socialId) {
        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .map(UserResponse::from);
    }

    /**
     * 사용자 ID로 사용자 조회
     * UserId VO로 변환 후 Repository 조회
     * 조회 결과를 UserResponse DTO로 변환하여 반환
     * 읽기 전용 트랜잭션 적용
     *
     * @param userId 사용자 고유 ID 문자열
     * @return 사용자 정보 Optional
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(String userId) {
        return userRepository.findById(UserId.of(userId))
                .map(UserResponse::from);
    }

    /**
     * 회원탈퇴 처리
     * 소셜 연결 해제 + 관련 데이터 삭제 + 계정 삭제를 순차적으로 처리
     * 
     * @param command 회원탈퇴 커맨드
     */
    public void withdrawUser(UserWithdrawalCommand command) {
        log.info("회원탈퇴 시작: userId={}", command.getUserId());
        
        // 1. 사용자 조회
        User user = userRepository.findById(UserId.of(command.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        try {
            // 2. 소셜 플랫폼 연결 해제 (실패해도 계속 진행)
            socialUnlinkService.unlinkSocialConnection(
                    user.getSocialType(), 
                    user.getSocialId()
            );
        } catch (Exception e) {
            log.warn("소셜 연결 해제 실패, 계속 진행: userId={}, error={}", 
                    command.getUserId(), e.getMessage());
        }
        
        try {
            // 3. 관련 데이터 삭제
            deleteUserRelatedData(user);
            
            // 4. 사용자 계정 삭제
            userRepository.delete(user);
            
            log.info("회원탈퇴 완료: userId={}, reason={}", 
                    command.getUserId(), command.getReason());
                    
        } catch (Exception e) {
            log.error("회원탈퇴 실패: userId={}, error={}", command.getUserId(), e.getMessage());
            throw new CustomException(ErrorCode.USER_WITHDRAWAL_FAILED);
        }
    }

    /**
     * 사용자 관련 데이터 삭제
     * RefreshToken 등 사용자와 연관된 모든 데이터를 정리
     * 
     * @param user 삭제할 사용자
     */
    private void deleteUserRelatedData(User user) {
        // RefreshToken 삭제
        refreshTokenRepository.deleteByUserId(user.getId().getValue());
        
        // TODO: 추후 추가 데이터 삭제
        // - 사용자 습관 데이터
        // - 사용자 설정 데이터
        // - 기타 연관 데이터
        
        log.info("사용자 관련 데이터 삭제 완료: userId={}", user.getId().getValue());
    }
}
