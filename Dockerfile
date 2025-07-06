# 멀티스테이지 빌드로 이미지 크기 최적화
FROM gradle:8.4-jdk17-alpine AS builder

WORKDIR /app

# Gradle 캐시 최적화를 위해 설정 파일들 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle build -x test --no-daemon

# 런타임 이미지
FROM openjdk:17-jdk-alpine

VOLUME /tmp

# 애플리케이션 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출 (docker-compose에서 8086 사용하므로)
EXPOSE 8086

# 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]