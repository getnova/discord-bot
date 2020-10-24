FROM adoptopenjdk:15-jdk-hotspot AS builder

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
RUN chmod +x ./gradlew && ./gradlew installDist --stacktrace --no-daemon

FROM adoptopenjdk:15-jre-hotspot

ARG BOT_USER=nova-bot
ARG BOT_GROUP=nova-bot

ENV BOT_HOME /opt/nova-bot
ENV DATA_DIR /data

RUN set -o errexit -o nounset \
    && groupadd --system --gid 1000 ${BOT_GROUP} \
    && useradd --system --gid ${BOT_GROUP} --uid 1000 --shell /bin/bash --create-home ${BOT_USER} \
    && mkdir -p ${DATA_DIR} \
    && chown --recursive ${BOT_USER}:${BOT_GROUP} ${DATA_DIR} \
    && chown --recursive ${BOT_USER}:${BOT_GROUP} /home/${BOT_USER}

WORKDIR ${DATA_DIR}
COPY --chown=${BOT_USER}:${BOT_GROUP} --from=builder /home/src/build/install/discord-bot/ ${BOT_HOME}/

RUN ln --symbolic ${BOT_HOME}/bin/discord-bot /usr/bin/nova-bot \
  && chmod +x /usr/bin/nova-bot
USER ${BOT_USER}

ENTRYPOINT ["nova-bot"]
