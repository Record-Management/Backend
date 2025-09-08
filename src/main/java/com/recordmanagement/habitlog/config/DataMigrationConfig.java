package com.recordmanagement.habitlog.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataMigrationConfig {
    
    private static final Logger log = LoggerFactory.getLogger(DataMigrationConfig.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    public DataMigrationConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @PostConstruct
    public void migrateRecordTime() {
        try {
            // record_time 컬럼이 있는지 확인
            String checkColumnSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = DATABASE() 
                AND TABLE_NAME = 'records' 
                AND COLUMN_NAME = 'record_time'
                """;
            
            int columnExists = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);
            
            if (columnExists > 0) {
                // record_time이 NULL인 기존 데이터 업데이트
                String updateSql = """
                    UPDATE records 
                    SET record_time = TIME(created_at) 
                    WHERE record_time IS NULL
                    """;
                
                int updatedRows = jdbcTemplate.update(updateSql);
                
                if (updatedRows > 0) {
                    log.info("기존 기록 {}개의 record_time을 created_at에서 추출하여 업데이트했습니다.", updatedRows);
                } else {
                    log.info("업데이트할 기록이 없습니다. (모든 기록에 이미 record_time이 설정되어 있음)");
                }
            } else {
                log.info("record_time 컬럼이 아직 생성되지 않았습니다. JPA가 컬럼을 생성한 후 다시 실행됩니다.");
            }
            
        } catch (Exception e) {
            log.error("record_time 데이터 마이그레이션 중 오류 발생: {}", e.getMessage());
        }
    }
}