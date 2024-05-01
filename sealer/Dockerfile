FROM gradle:latest AS builder
WORKDIR /app
COPY settings.gradle.kts  .
COPY sealer sealer
COPY smartContracts smartContracts
COPY gradle gradle
COPY gradlew gradlew
RUN ./gradlew build

FROM openjdk:17
WORKDIR /app
COPY --from=builder /app/sealer/build/libs/*.jar sealer.jar
CMD ["java", "-jar", "sealer.jar"]

