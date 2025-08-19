package com.recordmanagement.habitlog.domain.record.repository;

import com.recordmanagement.habitlog.domain.record.model.DailyRecord;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일상 기록 저장소 인터페이스
 * 
 * 일상 기록 도메인 객체의 영속성을 관리하는 리포지토리 인터페이스입니다.
 * 도메인 계층에서 정의되어 인프라스트럭처 계층에서 구현됩니다.
 * 
 * 설계 원칙:
 * - 도메인 중심 설계 (Domain-Driven Design) 적용
 * - 인프라스트럭처 세부사항으로부터 도메인 로직 분리
 * - 순수한 도메인 객체만 사용 (JPA Entity 등 사용 금지)
 * - 비즈니스 의미가 있는 메서드명 사용
 * 
 * 구현 책임:
 * - JPA Repository를 활용한 실제 구현체 제공
 * - 도메인 객체와 JPA Entity 간 매핑 처리
 * - 데이터베이스 트랜잭션 관리
 * - 쿼리 최적화 및 성능 관리
 * 
 * 데이터 일관성:
 * - 사용자당 하루 하나의 기록만 보장 (UNIQUE 제약조건)
 * - 참조 무결성 유지 (외래키 제약조건)
 * - 논리적 삭제보다 물리적 삭제 사용
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
public interface DailyRecordRepository {
    
    /**
     * 일상 기록 저장 (생성 및 수정)
     * 
     * 새로운 일상 기록을 생성하거나 기존 기록을 수정할 때 사용됩니다.
     * INSERT/UPDATE 구분은 엔티티의 ID 존재 여부로 자동 판단됩니다.
     * 
     * @param dailyRecord 저장할 일상 기록 도메인 객체
     * @return DailyRecord 저장된 일상 기록 (생성/수정 시간 포함)
     * @throws DataIntegrityViolationException 중복 기록 저장 시 (사용자당 하루 하나 제한)
     */
    DailyRecord save(DailyRecord dailyRecord);
    
    /**
     * 고유 식별자로 일상 기록 조회
     * 
     * @param id 일상 기록 고유 식별자 (UUID 문자열)
     * @return Optional<DailyRecord> 조회된 일상 기록 (없으면 empty)
     */
    Optional<DailyRecord> findById(String id);
    
    /**
     * 사용자의 특정 날짜 일상 기록 조회
     * 
     * 달력에서 특정 날짜를 선택했을 때 해당 날짜의 일상 기록을 조회합니다.
     * 하루에 하나의 기록만 허용되므로 Optional로 반환합니다.
     * 
     * @param userId 조회할 사용자 ID
     * @param recordDate 조회할 기록 날짜 (YYYY-MM-DD)
     * @return Optional<DailyRecord> 해당 날짜의 일상 기록 (없으면 empty)
     */
    Optional<DailyRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    /**
     * 사용자의 기간별 일상 기록 목록 조회
     * 
     * 월별 캘린더 표시나 통계 데이터 생성 시 사용됩니다.
     * 시작 날짜와 종료 날짜를 포함하여 조회합니다 (inclusive).
     * 
     * @param userId 조회할 사용자 ID  
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate 조회 종료 날짜 (포함)
     * @return List<DailyRecord> 기간 내 일상 기록 목록 (날짜 순 정렬)
     */
    List<DailyRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 일상 기록 삭제
     * 
     * 사용자가 일상 기록을 삭제하거나 회원탈퇴 시 관련 데이터를 정리할 때 사용됩니다.
     * 물리적 삭제를 수행하며, 삭제된 데이터는 복구할 수 없습니다.
     * 
     * @param dailyRecord 삭제할 일상 기록 도메인 객체
     * @throws EntityNotFoundException 삭제하려는 기록이 존재하지 않는 경우
     */
    void delete(DailyRecord dailyRecord);
}