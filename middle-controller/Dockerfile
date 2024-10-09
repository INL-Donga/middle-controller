# 1. 단계: 빌드 환경 설정
FROM openjdk:17-jdk-alpine AS build

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. 프로젝트의 모든 파일 복사
COPY . .
RUN chmod +x ./gradlew
# 4. 프로젝트 빌드 (Gradle 사용)
RUN ./gradlew clean build -x test

# 5. 2단계: 실행 환경 설정
FROM openjdk:17-jdk

# 6. 빌드 단계에서 생성된 .jar 파일을 복사
COPY --from=build /app/build/libs/middle-controller-1.0-SNAPSHOT.jar /app/test2.jar

# 7. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/test2.jar"]
