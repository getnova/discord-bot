<h1 align="center">
    Nova Discord Bot
</h1>
<p align="center">
    <a style="text-decoration:none" href="https://github.com/getnova/discord-bot/releases">
        <img alt="Releases" src="https://img.shields.io/github/v/tag/getnova/discord-bot?label=latest%20version&style=flat-square">
    </a>
    <a style="text-decoration:none" href="https://github.com/getnova/discord-bot/actions">
        <img alt="build" src="https://img.shields.io/github/workflow/status/getnova/discord-bot/CI?label=build&style=flat-square">
    </a>
    <a style="text-decoration:none" href="https://hub.docker.com/r/getnova/discord-bot">
        <img alt="DockerHub" src="https://img.shields.io/docker/pulls/getnova/discord-bot?style=flat-square">
    </a>
</p>
<p align="center">
    This is the Discord Bot of the Nova Project.
</p>

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing
purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install the software and how to install them

* A Java IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea/))
* [JDK 14](https://adoptopenjdk.net/index.html) or higher
* [Git](https://git-scm.com/)

### Installing

A step by step series of examples that tell you how to get a development env running

```sh
git clone https://github.com/getnova/discord-bot
```

Then you can open it with you IDE and start contributing.

#### Code Style

* [IntelliJ Google Code Style](https://raw.githubusercontent.com/google/styleguide/gh-pages/intellij-java-google-style.xml)

### Deployment

```yaml
# docker-compose.yaml
version: "3.9"

services:

  discord-bot:
    image: getnova/discord-bot:latest
    restart: always
    environment:
      - "DEBUG=true"
      - "SQL_SERVER_LOCATION=postgresql://database:5432"
      - "SQL_SERVER_PASSWORD=nova"
      - "SQL_SERVER_USERNAME=nova"
      - "SQL_SERVER_DATABASE=nova"
      - "DISCORD_BOT_TOKEN=<Your-Bot-Token>"

  database:
    image: postgres:alpine
    restart: always
    environment:
      - "POSTGRES_PASSWORD=nova"
      - "POSTGRES_USER=nova"
      - "POSTGRES_DB=nova"
      - "PGDATA=/var/lib/postgresql/data"
    volumes:
      - "./db-data:/var/lib/postgresql/data"
```

## Environment

| Name                 | Default Value | Description                                                                                                 |
|----------------------|---------------|-------------------------------------------------------------------------------------------------------------|
| `DISCORD_BOT_TOKEN`  |               | The discord bot token; you can obtain one here: [discord.com/developers](https://discord.com/developers)    |
| `DISCORD_BOT_PREFIX` | `!`           | The prefix witch is in every message that should be interpreted as a bot command.                           |

See [getnova/nova-frontend#environment](https://github.com/getnova/nova-framework#environment)

## Built With

* [Gradle](https://gradle.org/) - The build tool
* [JUnit](https://junit.org/) - The test tool

## License

| Licenses                                                                                                              |
|-----------------------------------------------------------------------------------------------------------------------|
| From 08.08.2020 [AGPL v3](LICENSE)                                                                                    |
| Upto 07.08.2020 [MIT](https://github.com/getnova/discord-bot/blob/bae6598a095699e1e3ffae7b8d98d8e7c83247cf/LICENSE)   |
