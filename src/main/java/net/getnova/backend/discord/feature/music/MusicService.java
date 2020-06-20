package net.getnova.backend.discord.feature.music;

import net.dv8tion.jda.api.JDA;
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

@Service(value = "discordMusic", depends = {DiscordBot.class, DashboardService.class, CommandService.class, AudioService.class})
@Singleton
public class MusicService {

    private MusicDashboard dashboard;

    @Inject
    private DashboardService dashboardService;
    @Inject
    private CommandService commandService;
    @Inject
    private InjectionHandler injectionHandler;

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.commandService.addCommand(new PlayCommand());
        this.commandService.addCommand(new StopCommand());
        this.commandService.addCommand(new SkipCommand());
        this.commandService.addCommand(new PlaylistCommand());
    }

    @InitService
    private void init(final InitServiceEvent event) {
        this.dashboard = new MusicDashboard(this.injectionHandler.getInjector().getInstance(JDA.class).getTextChannelById(719177733778964571L));
        this.dashboardService.addDashboard(dashboard);
    }

    public void updateDashboard() {
        this.dashboard.update();
    }
}
