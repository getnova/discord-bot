FROM adoptopenjdk:14-jdk-hotspot AS builder

ARG GRADLE_USER=gradle
ARG GRADLE_GROUP=gradle

RUN set -o errexit -o nounset \
    && groupadd --system --gid 1000 ${GRADLE_GROUP} \
    && useradd --system --gid ${GRADLE_GROUP} --uid 1000 --shell /bin/bash --create-home ${GRADLE_USER} \
    && chown --recursive ${GRADLE_USER}:${GRADLE_GROUP} /home/${GRADLE_USER} \
    && mkdir -p /home/src \
    && chown --recursive ${GRADLE_USER}:${GRADLE_GROUP} /home/src \
    && apt-get update \
    && apt-get install git -y

COPY --chown=${GRADLE_USER}:${GRADLE_GROUP} . /home/src
WORKDIR /home/src

USER ${GRADLE_USER}
RUN chmod +x ./gradlew && ./gradlew :build --stacktrace --no-daemon

FROM getnova/nova-backend:latest

COPY --from=builder --chown=nova:nova /home/src/build/libs/ /data/modules/
