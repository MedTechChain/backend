FROM gradle:8.6.0-jdk21 as builder

COPY --chown=gradle:gradle . /home/gradle/src

RUN test -d /home/gradle/src/crypto/ # "Crypto material not provided"

WORKDIR /home/gradle/src

RUN gradle build --no-daemon

FROM eclipse-temurin:21-jdk-jammy

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
COPY --from=builder /home/gradle/src/crypto/ /crypto/

WORKDIR /app

EXPOSE 8080

CMD ["java", "-jar", "/app/spring-boot-application.jar"]