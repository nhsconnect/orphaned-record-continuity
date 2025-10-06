FROM node:22.5-alpine AS builder

COPY package*.json /app/

WORKDIR /app

RUN npm install
RUN npm ci --only=production

# production app image
FROM alpine:3.15

# take just node without npm (including npx) or yarn
COPY --from=builder /usr/local/bin/node /usr/local/bin

# take built node_modules
COPY --from=builder /app /app

RUN apk update && apk add openssl ca-certificates git && rm -rf /var/cache/apk/*
RUN apk add --no-cache \
        python3 \
        py3-pip \
    && pip3 install --upgrade pip \
    && pip3 install \
        awscli \
    && rm -rf /var/cache/apk/*


COPY ./certs/deductions.crt /usr/local/share/ca-certificates/deductions.crt
RUN update-ca-certificates

ENV NODE_EXTRA_CA_CERTS=/usr/local/share/ca-certificates/deductions.crt

EXPOSE 3000
RUN apk add --no-cache tini bash

COPY build/                   /app/
COPY scripts/load-api-keys.sh /app/scripts/load-api-keys.sh
COPY run-server.sh            /usr/bin/run-gp2gp-server

ENV GP2GP_MESSENGER_REPOSITORY_ASID=deduction-asid \
  GP2GP_MESSENGER_REPOSITORY_ODS_CODE=deduction-ods \
  NHS_ENVIRONMENT=local \
  GP2GP_MESSENGER_MHS_OUTBOUND_URL=""

WORKDIR /app

ARG UTILS_VERSION
RUN test -n "$UTILS_VERSION"
COPY utils/$UTILS_VERSION/run-with-redaction.sh ./utils/
COPY utils/$UTILS_VERSION/redactor              ./utils/

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["/app/utils/run-with-redaction.sh", "/usr/bin/run-gp2gp-server"]

RUN addgroup -g 1000 node \
    && adduser -u 1000 -G node -s /bin/sh -D node

USER node
