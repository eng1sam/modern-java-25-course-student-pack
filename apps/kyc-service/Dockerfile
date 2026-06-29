# syntax=docker/dockerfile:1
# Multi-stage build: compile with a Maven+JDK image, run on a slim JRE.
# Using the Maven image (rather than the wrapper) keeps the build image free of
# extra tooling and avoids re-downloading Maven on every build.

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
# Resolve dependencies first so they cache independently of source changes.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2,id=kyc-service-m2 mvn -B -ntp -DskipTests dependency:go-offline || true
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2,id=kyc-service-m2 mvn -B -ntp -DskipTests clean package

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /work
# Run as a non-root user.
RUN groupadd --system app && useradd --system --gid app --home /work app
# Quarkus fast-jar layout — copy the rarely-changing libs first for better caching.
COPY --from=build /build/target/quarkus-app/lib/ lib/
COPY --from=build /build/target/quarkus-app/*.jar ./
COPY --from=build /build/target/quarkus-app/app/ app/
COPY --from=build /build/target/quarkus-app/quarkus/ quarkus/
USER app
EXPOSE 8080
ENV QUARKUS_PROFILE=prod
ENTRYPOINT ["java", "-jar", "/work/quarkus-run.jar"]
