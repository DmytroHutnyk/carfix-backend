# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

# Wrapper first as it changes far less often than sources
COPY mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Every module POM before any source, so the dependency layer survives code edits.
COPY pom.xml ./
COPY boot/pom.xml boot/
COPY common/pom.xml common/
COPY applicationHexagon/domain/pom.xml applicationHexagon/domain/
COPY applicationHexagon/application/pom.xml applicationHexagon/application/
COPY ports/pom.xml ports/
COPY adapters/adaptersIn/pom.xml adapters/adaptersIn/
COPY adapters/adaptersOut/pom.xml adapters/adaptersOut/

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q dependency:go-offline -DskipTests -Dmaven.gitcommitid.skip=true || true

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B clean package -DskipTests -Dmaven.gitcommitid.skip=true \
 && cp boot/target/boot-*.jar /build/app.jar

FROM eclipse-temurin:25-jre AS run
WORKDIR /app

RUN groupadd -r carfix && useradd -r -g carfix carfix

COPY --from=build --chown=carfix:carfix /build/app.jar app.jar

USER carfix
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
