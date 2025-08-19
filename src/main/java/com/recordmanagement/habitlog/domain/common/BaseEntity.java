package com.recordmanagement.habitlog.domain.common;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 모든 도메인 엔티티의 공통 필드를 관리하는 기본 엔티티
 * 
 * 모든 도메인 엔티티가 가져야 하는 공통 속성들을 정의합니다.
 * ID 생성, 생성/수정 시간 관리 로직을 중앙화하여 코드 중복을 제거합니다.
 * 
 * 공통 필드:
 * - id: 고유 식별자 (UUID 자동 생성)
 * - createdAt: 생성 시간 (자동 설정)
 * - updatedAt: 수정 시간 (자동 갱신)
 * 
 * JPA 라이프사이클 이벤트:
 * - @PrePersist: 엔티티 저장 전 ID와 생성시간 자동 설정
 * - @PreUpdate: 엔티티 수정 전 수정시간 자동 갱신
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity {

    /**
     * 고유 식별자
     * UUID 문자열 형태로 자동 생성됩니다.
     */
    @Id
    protected String id;

    /**
     * 엔티티 생성 시간
     * 최초 저장 시 자동으로 설정되며 이후 변경되지 않습니다.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    /**
     * 엔티티 수정 시간  
     * 저장/수정 시마다 현재 시간으로 자동 갱신됩니다.
     */
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    /**
     * JPA 엔티티 저장 전 이벤트 처리
     * 
     * 새로운 엔티티가 데이터베이스에 저장되기 전에 호출됩니다.
     * 고유 ID 생성과 생성/수정 시간을 현재 시간으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA 엔티티 수정 전 이벤트 처리
     * 
     * 기존 엔티티가 수정되어 데이터베이스에 업데이트되기 전에 호출됩니다.
     * 수정 시간을 현재 시간으로 갱신합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 수정 시간을 수동으로 갱신
     * 
     * 도메인 로직에서 명시적으로 수정 시간을 갱신해야 할 때 사용합니다.
     * 비즈니스 로직 수행 후 변경사항을 추적하기 위한 용도입니다.
     */
    protected void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}