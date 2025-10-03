FROM python:3.9.6-alpine

ENV POLL_FREQUENCY=60
ENV FORWARDER_HOME=/mesh-forwarder
ARG IMAGE_TAG
ENV BUILD_TAG=${IMAGE_TAG}

RUN addgroup --system mesh && adduser --ingroup mesh --system mesh

COPY . /mesh-forwarder

RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

RUN cd /mesh-forwarder && pip install -r requirements.txt

RUN chown -R mesh:mesh /mesh-forwarder

USER mesh

ENTRYPOINT ["python", "-m", "awsmesh.entrypoint"]
