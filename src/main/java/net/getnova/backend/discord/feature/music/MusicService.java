package net.getnova.backend.discord.feature.music;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.CommandService;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.discord.feature.music.commands.PlayCommand;
import net.getnova.backend.discord.feature.music.commands.PlaylistCommand;
import net.getnova.backend.discord.feature.music.commands.SkipCommand;
import net.getnova.backend.discord.feature.music.commands.StopCommand;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.InitService;
import net.getnova.backend.service.event.InitServiceEvent;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "discordMusic", depends = {DiscordBot.class, DashboardService.class, CommandService.class, AudioService.class})
@Singleton
public class MusicService {

    private final Map<Long, MusicDashboard> dashboards;

    @Inject
    private DashboardService dashboardService;
    @Inject
    private CommandService commandService;
    @Inject
    private InjectionHandler injectionHandler;

    public MusicService() {
        this.dashboards = new HashMap<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.commandService.addCommand(new PlayCommand());
        this.commandService.addCommand(new StopCommand());
        this.commandService.addCommand(new SkipCommand());
        this.commandService.addCommand(new PlaylistCommand());
    }

    @InitService
    private void init(final InitServiceEvent event) {
        final List<Guild> guilds = this.injectionHandler.getInjector().getInstance(JDA.class).getGuilds();

        guilds.forEach(guild -> {
            final List<TextChannel> musicChannels = guild.getTextChannelsByName("music", false);
            if (!musicChannels.isEmpty()) this.addDashboard(new MusicDashboard(musicChannels.get(0)));
        });
    }

    private void addDashboard(final MusicDashboard dashboard) {
        this.dashboards.put(dashboard.getGuild().getIdLong(), dashboard);
        this.dashboardService.addDashboard(dashboard);
    }

    public void updateDashboard(final Guild guild) {
        this.dashboards.get(guild.getIdLong()).update();
    }
}
