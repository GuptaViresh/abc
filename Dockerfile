# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew bootJar -x test --no-daemon 2>/dev/null || \
    (mvn package -q -DskipTests 2>/dev/null) || \
    echo "Build tool not found; skipping build"

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /workspace/build/libs/*.jar app.jar 2>/dev/null || \
     COPY --from=builder /workspace/target/*.jar app.jar 2>/dev/null || true
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
