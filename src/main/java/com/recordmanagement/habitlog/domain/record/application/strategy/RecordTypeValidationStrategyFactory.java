package com.recordmanagement.habitlog.domain.record.application.strategy;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 기록 타입 검증 전략 팩토리
 * 
 * OCP 적용: Factory 패턴을 통한 전략 관리
 * - 새로운 기록 타입 추가 시 자동으로 등록
 * - Spring의 의존성 주입을 활용한 동적 전략 수집
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
@Slf4j
@Component
public class RecordTypeValidationStrategyFactory {

    private final Map<RecordType, RecordTypeValidationStrategy> strategies;

    /**
     * Spring이 모든 RecordTypeValidationStrategy 구현체를 자동으로 주입
     * 새로운 전략이 추가되면 자동으로 등록됨 (OCP 준수)
     */
    public RecordTypeValidationStrategyFactory(List<RecordTypeValidationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        RecordTypeValidationStrategy::getSupportedType,
                        Function.identity()
                ));
        
        log.info("기록 타입 검증 전략 등록 완료: {}", 
                strategies.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));
    }

    /**
     * 기록 타입에 해당하는 전략 반환
     * 
     * @param recordType 기록 타입
     * @return 해당 기록 타입의 검증 전략
     * @throws IllegalArgumentException 지원하지 않는 기록 타입인 경우
     */
    public RecordTypeValidationStrategy getStrategy(RecordType recordType) {
        RecordTypeValidationStrategy strategy = strategies.get(recordType);
        
        if (strategy == null) {
            throw new IllegalArgumentException(
                    String.format("지원하지 않는 기록 타입: %s. 지원 타입: %s", 
                            recordType, 
                            strategies.keySet().stream().map(Enum::name).collect(Collectors.joining(", "))
                    )
            );
        }
        
        return strategy;
    }

    /**
     * 지원하는 모든 기록 타입 반환
     * 
     * @return 지원 가능한 기록 타입 목록
     */
    public List<RecordType> getSupportedTypes() {
        return List.copyOf(strategies.keySet());
    }
}