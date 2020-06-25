package net.getnova.backend.discord;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.getnova.backend.Nova;
import net.getnova.backend.config.ConfigService;
import net.getnova.backend.service.Service;
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

    @Getter
    private final DiscordBotConfig config;
    private JDA jda;

    @Inject
    private Nova nova;

    @Inject
    public DiscordBot(final ConfigService configService) {
        this.config = configService.addConfig("discord-bot", new DiscordBotConfig());
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) throws LoginException {
        this.jda = JDABuilder.create(this.config.getToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .setAutoReconnect(true)
                .setRequestTimeoutRetry(true)
                .setActivity(Activity.watching("people" + this.config.getPrefix()))
                .setAudioSendFactory(new NativeAudioSendFactory())
                .build();
        event.getBinder().bind(JDA.class).toInstance(this.jda);
        try {
            this.jda.awaitReady();
        } catch (InterruptedException ignored) {
        }
    }

    @StopService
    private void stop(final StopServiceEvent event) {
        this.jda.shutdown();
    }
}
