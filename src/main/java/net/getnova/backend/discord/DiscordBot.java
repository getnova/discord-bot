package net.getnova.backend.discord;

import net.getnova.backend.config.ConfigService;
import net.getnova.backend.service.Service;

import javax.inject.Inject;

@Service(value = "discord-bot", depends = ConfigService.class)
public class DiscordBot {

    private final DiscordBotConfig config;

    @Inject
    public DiscordBot(final ConfigService configService) {
        this.config = configService.addConfig("discordBot", new DiscordBotConfig());
    }
}
