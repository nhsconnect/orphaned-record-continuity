#!/bin/bash
java -Dlog4j2.formatMsgNoLookups=true -XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=80.0 -jar /app.jar
