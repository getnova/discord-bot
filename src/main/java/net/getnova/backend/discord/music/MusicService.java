package net.getnova.backend.discord.music;

import net.dv8tion.jda.api.JDA;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.CommandService;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.InitService;
import net.getnova.backend.service.event.InitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Service(value = "discordMusic", depends = {DiscordBot.class, DashboardService.class, CommandService.class, AudioService.class})
@Singleton
public class MusicService {

    @Inject
    private DashboardService dashboardService;
    @Inject
    private InjectionHandler injectionHandler;

    @InitService
    private void init(final InitServiceEvent event) {
        this.dashboardService.addDashboard(new MusicDashboard(
                this.injectionHandler.getInjector().getInstance(JDA.class).getTextChannelById(719177733778964571L)
        ));
    }
}
