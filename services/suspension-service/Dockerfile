FROM eclipse-temurin:21.0.1_12-jre-alpine

RUN apk add --no-cache bash

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY build/libs/*.jar app.jar
COPY run-application.sh ./

ENTRYPOINT ["./run-application.sh"]
