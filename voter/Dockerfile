FROM gradle:latest AS builder
WORKDIR /app
COPY settings.gradle.kts  .
COPY voter voter
COPY smartContracts smartContracts
COPY gradle gradle
COPY gradlew gradlew
RUN ./gradlew build

FROM openjdk:17
WORKDIR /app
COPY --from=builder /app/voter/build/libs/*.jar voter.jar
CMD ["java", "-jar", "voter.jar"]
