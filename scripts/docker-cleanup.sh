#!/bin/bash

# 안전한 Docker 정리 스크립트
# DB 볼륨과 실행 중인 컨테이너는 보호

echo "=== 안전한 Docker 정리 시작 ==="

# 1. 중지된 컨테이너만 제거 (실행 중인 MySQL은 보호됨)
echo "중지된 컨테이너 정리 중..."
docker container prune -f

# 2. 사용하지 않는 네트워크 제거 (실행 중인 컨테이너가 사용하는 것은 보호됨)
echo "사용하지 않는 네트워크 정리 중..."
docker network prune -f

# 3. 댕글링 이미지만 제거 (tag가 없는 이미지들)
echo "댕글링 이미지 정리 중..."
docker image prune -f

# 4. 빌드 캐시 정리
echo "빌드 캐시 정리 중..."
docker builder prune -f

# 5. 이전 버전 앱 이미지만 제거 (최신 2개 버전 유지)
echo "이전 앱 이미지 정리 중..."
APP_IMAGES=$(docker images --format "table {{.Repository}}:{{.Tag}}" | grep "habitlog-backend" | tail -n +3)
if [ ! -z "$APP_IMAGES" ]; then
    echo "$APP_IMAGES" | xargs docker rmi -f 2>/dev/null || true
fi

# 6. 정리 결과 출력
echo "=== 정리 완료 ==="
echo "현재 디스크 사용량:"
df -h | grep -E '(Filesystem|/dev/)'

echo "Docker 사용량:"
docker system df

echo "실행 중인 컨테이너:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo "=== 안전한 Docker 정리 완료 ==="