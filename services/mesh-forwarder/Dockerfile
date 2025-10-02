ARG SOURCE_IMAGE_TAG
FROM registrations/mesh-inbox-s3-forwarder:$SOURCE_IMAGE_TAG

RUN whoami
USER root
RUN apk --no-cache add bash
USER mesh

ARG UTILS_VERSION
RUN test -n "$UTILS_VERSION"
COPY utils/$UTILS_VERSION/run-with-redaction.sh ./utils/
COPY utils/$UTILS_VERSION/redactor              ./utils/

ENTRYPOINT ["./utils/run-with-redaction.sh", "python", "-m", "awsmesh.entrypoint"]
