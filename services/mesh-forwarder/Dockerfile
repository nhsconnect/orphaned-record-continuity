ARG IMAGE_TAG=image_tag \
    ENV=bucket
FROM ${ENV}.dkr.ecr.eu-west-2.amazonaws.com/deductions/mesh-inbox-s3-forwarder:latest

RUN whoami
USER root
RUN apk --no-cache add bash
USER mesh

ENTRYPOINT ["python", "-m", "awsmesh.entrypoint"]
