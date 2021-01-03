FROM adoptopenjdk:15-jdk-hotspot-bionic AS builder

RUN set -o errexit -o nounset \
    && apt-get update \
    && apt-get install git -y

COPY . /home/src
WORKDIR /home/src

RUN chmod +x ./gradlew \
    && ./gradlew installDist --stacktrace --no-daemon

FROM adoptopenjdk:15-jre-hotspot-bionic

ARG NOVA_USER=nova
ARG NOVA_GROUP=nova

ENV NOVA_HOME /opt/discord-bot
ENV DATA_DIR /data

RUN set -o errexit -o nounset \
    && groupadd --system --gid 1000 ${NOVA_GROUP} \
    && useradd --system --gid ${NOVA_GROUP} --uid 1000 --shell /bin/bash --create-home ${NOVA_USER} \
    && mkdir -p ${DATA_DIR} \
    && chown --recursive ${NOVA_USER}:${NOVA_GROUP} ${DATA_DIR} \
    && chown --recursive ${NOVA_USER}:${NOVA_GROUP} /home/${NOVA_USER}

WORKDIR ${DATA_DIR}
COPY --from=builder --chown=${NOVA_USER}:${NOVA_GROUP} /home/src/build/install/discord-bot/ ${NOVA_HOME}/

RUN ln --symbolic ${NOVA_HOME}/bin/discord-bot /usr/bin/discord-bot \
  && chmod +x /usr/bin/discord-bot
USER ${NOVA_USER}

ENTRYPOINT ["discord-bot", "net.getnova.module.discord.Discord"]
