package net.getnova.discordbot.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.getnova.discordbot.service.audio.AudioService;
import net.getnova.discordbot.service.command.CommandService;
import net.getnova.discordbot.service.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;

@Slf4j
@Service
public class JDAService {

    private final CommandService commandService;
    private final AudioService audioService;
    private final MenuService menuService;

    @Value("${discord.token}")
    private String token;

    @Value("${discord.prefix}")
    private String prefix;

    @Getter
    private JDA jda;

    @Autowired
    public JDAService(final CommandService commandService, final AudioService audioService, final MenuService menuService) {
        this.commandService = commandService;
        this.audioService = audioService;
        this.menuService = menuService;
    }

    @PostConstruct
    private void init() {
        try {
            this.jda = JDABuilder.create(this.token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS)
                    .setAutoReconnect(true)
                    .setRequestTimeoutRetry(true)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.watching("you" + this.prefix))
                    .addEventListeners(this.commandService.getHandler(), this.audioService, this.menuService)
                    .build();

        } catch (LoginException e) {
            log.error("Error while logging in at Discord: {}", e.getMessage());
        }
    }

    @PreDestroy
    private void onDestroy() {
        this.jda.shutdown();
    }
}
