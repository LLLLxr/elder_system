# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
# 需要父 POM，此处假设通过 Maven Wrapper 在项目根构建后 COPY jar
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/app.jar app.jar

# 非 root 用户运行
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

EXPOSE 8082
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

