# 1단계: Build stage
FROM gradle:8.4.0-jdk17 AS builder
WORKDIR /app

# JVM 메모리 설정
ENV GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Gradle 캐시를 재사용하기 위해 gradle 관련 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || return 0  # 의존성만 다운로드

# 전체 프로젝트 복사 후 빌드
COPY . .
RUN gradle clean build -x test --no-daemon --stacktrace

# 2단계: Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# build/libs 디렉토리에서 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]