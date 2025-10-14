FROM eclipse-temurin:11.0.17_8-jre-alpine

RUN apk add --no-cache bash

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY build/libs/*.jar app.jar
COPY run-application.sh ./

ARG UTILS_VERSION
RUN test -n "$UTILS_VERSION"
COPY utils/$UTILS_VERSION/run-with-redaction.sh ./utils/
COPY utils/$UTILS_VERSION/redactor              ./utils/

ENTRYPOINT ["./utils/run-with-redaction.sh", "./run-application.sh"]
