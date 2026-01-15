FROM eclipse-temurin:11-jre-alpine-3.23

RUN apk add --no-cache bash

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY build/libs/*.jar app.jar
COPY run-application.sh ./

ENTRYPOINT ["./run-application.sh"]
