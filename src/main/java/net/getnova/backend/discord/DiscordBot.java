package net.getnova.backend.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.getnova.backend.config.ConfigService;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.InitService;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;
import net.getnova.backend.service.event.StopService;
import net.getnova.backend.service.event.StopServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;

@Service(value = "discord-bot", depends = ConfigService.class)
@Singleton
public class DiscordBot {

    private final DiscordBotConfig config;
    private JDA jda;

    @Inject
    public DiscordBot(final ConfigService configService) {
        this.config = configService.addConfig("discordBot", new DiscordBotConfig());
    }

    @PreInitService
    public void preInit(final PreInitServiceEvent event) throws LoginException {
        this.jda = JDABuilder.create(this.config.getToken(), GatewayIntent.GUILD_MESSAGES)
                .setAutoReconnect(true)
                .setActivity(Activity.watching("people!"))
                .build();
        event.getBinder().bind(JDA.class).toInstance(this.jda);
    }

    @StopService
    public void stop(final StopServiceEvent event) {
        this.jda.shutdown();
    }
}
