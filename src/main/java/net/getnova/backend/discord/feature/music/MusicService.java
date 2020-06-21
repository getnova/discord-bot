package net.getnova.backend.discord.feature.music;

import net.dv8tion.jda.api.entities.Guild;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.CommandService;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.discord.feature.music.commands.PlayCommand;
import net.getnova.backend.discord.feature.music.commands.SkipCommand;
import net.getnova.backend.discord.feature.music.commands.StopCommand;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Service(value = "discordMusic", depends = {DiscordBot.class, DashboardService.class, CommandService.class, AudioService.class})
@Singleton
public class MusicService {

    @Inject
    private DashboardService dashboardService;
    @Inject
    private CommandService commandService;

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.commandService.addCommand(new PlayCommand());
        this.commandService.addCommand(new StopCommand());
        this.commandService.addCommand(new SkipCommand());
        this.dashboardService.addDashboard(MusicDashboard.class);
    }

    public void updateDashboard(final Guild guild) {
        final MusicDashboard dashboard = this.dashboardService.getDashboard(guild, MusicDashboard.class);
        if (dashboard != null) dashboard.update();
    }
}
