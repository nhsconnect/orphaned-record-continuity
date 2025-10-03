FROM python:3.9.6-alpine

ENV POLL_FREQUENCY=60
ENV FORWARDER_HOME=/mesh-forwarder
ARG IMAGE_TAG
ENV BUILD_TAG=${IMAGE_TAG}

RUN addgroup --system mesh && adduser --ingroup mesh --system mesh
COPY . /mesh-forwarder
COPY requirements.txt /mesh-forwarder/requirements.txt
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
RUN pip install --user pipenv
RUN cd /mesh-forwarder && pipenv install

RUN chown -R mesh:mesh /mesh-forwarder

USER mesh

ENTRYPOINT ["python", "-m", "awsmesh.entrypoint"]
