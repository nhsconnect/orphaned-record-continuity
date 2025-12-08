# TODO PRM-495 reintroduce IMAGE_TAG later - We've hardcoded the last image tag as a quick fix. THIS SHOULD NOT BE A PERMANENT SOLUTION.
#ARG IMAGE_TAG=image_tag
FROM registrations/mesh-inbox-s3-forwarder:3c468a36

RUN whoami
USER root
RUN apk --no-cache add bash
USER mesh

ENTRYPOINT ["python", "-m", "awsmesh.entrypoint"]
