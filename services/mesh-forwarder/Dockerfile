FROM python:3.9.6-alpine

ENV POLL_FREQUENCY=60
ENV FORWARDER_HOME=/mesh-forwarder
ARG IMAGE_TAG=image_tag \
    ENV=bucket
ENV BUILD_TAG=${IMAGE_TAG}

RUN addgroup --system mesh && adduser --ingroup mesh --system mesh
COPY . /mesh-forwarder

RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
RUN pip install pipenv
RUN cd /mesh-forwarder && pipenv install

RUN chown -R mesh:mesh /mesh-forwarder

USER mesh

ENTRYPOINT ["python", "-m", "src.awsmesh.entrypoint"]
