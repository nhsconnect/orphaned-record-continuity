ARG IMAGE_TAG=image_tag \
    ENV=bucket
FROM 327778747031.dkr.ecr.eu-west-2.amazonaws.com/registrations/mesh-inbox-s3-forwarder:3c468a36

RUN whoami
USER root
RUN apk --no-cache add bash
USER mesh

ENTRYPOINT ["python", "-m", "awsmesh.entrypoint"]
