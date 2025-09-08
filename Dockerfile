# 1단계: Build stage
FROM gradle:8.10-jdk17-alpine AS builder

WORKDIR /app

# JVM 메모리 설정 (EC2 t3.small 최적화)
ENV GRADLE_OPTS="-Xmx1536m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -Dfile.encoding=UTF-8"

# Gradle Wrapper 및 설정 파일만 먼저 복사
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle/ gradle/

# 의존성 다운로드만 실행 (캐시 레이어)
RUN ./gradlew dependencies --no-daemon --quiet

# 소스 코드 복사
COPY src/ src/

# 빌드 실행
RUN ./gradlew clean build -x test --no-daemon --parallel

# 2단계: Runtime stage  
FROM eclipse-temurin:17-jre-alpine

# 보안: non-root 사용자 생성
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -G spring -u 1001

WORKDIR /app

# JAR 파일 복사 및 권한 설정
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# 비대화형 사용자로 전환
USER spring

# 메모리 최적화된 JVM 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
