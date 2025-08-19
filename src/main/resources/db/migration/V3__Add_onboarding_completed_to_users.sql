-- 사용자 테이블에 온보딩 완료 여부 컬럼 추가
ALTER TABLE users ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;