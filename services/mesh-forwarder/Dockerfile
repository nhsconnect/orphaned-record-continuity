ARG SOURCE_IMAGE_TAG
FROM registrations/mesh-inbox-s3-forwarder:$SOURCE_IMAGE_TAG

RUN whoami
USER root
RUN apk --no-cache add bash
USER mesh

ENTRYPOINT ["python", "-m", "awsmesh.entrypoint"]
