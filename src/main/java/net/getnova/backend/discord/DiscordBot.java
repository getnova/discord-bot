package net.getnova.backend.discord;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.getnova.backend.config.ConfigService;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.CommandService;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.ServiceHandler;
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
    @Getter
    private JDA jda;

    @Inject
    public DiscordBot(final ConfigService configService, final ServiceHandler serviceHandler) {
        this.config = configService.addConfig("discordBot", new DiscordBotConfig());
        serviceHandler.addService(CommandService.class);
        serviceHandler.addService(AudioService.class);
    }

    @PreInitService
    public void preInit(final PreInitServiceEvent event) throws LoginException {
        this.jda = JDABuilder.create(this.config.getToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .setAutoReconnect(true)
                .setActivity(Activity.watching("people!"))
                .build();
        event.getBinder().bind(JDA.class).toInstance(this.jda);
        try {
            this.jda.awaitReady();
        } catch (InterruptedException ignored) {
        }
    }

    @StopService
    public void stop(final StopServiceEvent event) {
        this.jda.shutdown();
    }
}
