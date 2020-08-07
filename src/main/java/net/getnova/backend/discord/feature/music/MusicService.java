package net.getnova.backend.discord.feature.music;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.audio.AudioPlayerSendHandler;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.CommandService;
import net.getnova.backend.discord.dashboard.Dashboard;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.discord.feature.music.commands.PlayCommand;
import net.getnova.backend.discord.feature.music.commands.SearchCommand;
import net.getnova.backend.discord.feature.music.commands.SkipCommand;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;
import net.getnova.backend.service.event.StartService;
import net.getnova.backend.service.event.StartServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service(id = "discord-music", depends = {DiscordBot.class, CommandService.class, EventService.class, DashboardService.class, AudioService.class})
@Singleton
@Slf4j
public final class MusicService {

    private final Timer timer;
    private final Map<Long, MusicPlayer> players;

    @Inject
    private CommandService commandService;
    @Inject
    private EventService eventService;
    @Inject
    private DashboardService dashboardService;
    @Inject
    private AudioService audioService;

    public MusicService() {
        this.timer = new Timer();
        this.players = new HashMap<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.commandService.addCommand(PlayCommand.class);
        this.commandService.addCommand(SearchCommand.class);
        this.commandService.addCommand(SkipCommand.class);
        this.eventService.addListener(MusicEvent.class);
        this.dashboardService.addDashboard(MusicDashboard.class);
    }

    @StartService
    private void start(final StartServiceEvent event) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    dashboardService.getDashboards(MusicDashboard.class).forEach(Dashboard::render);
                } catch (Exception e) {
                    log.error("Error while updating the music dashboard.", e);
                }
            }
        }, 10 * 1000, 10 * 1000);
    }

    public MusicPlayer getPlayer(final Guild guild) {
        MusicPlayer player = this.players.get(guild.getIdLong());

        if (player == null) {
            player = new MusicPlayer(this.audioService.getPlayerManager(), currentPayer -> this.dashboardService.getDashboard(guild, MusicDashboard.class).render());
            this.players.put(guild.getIdLong(), player);
        }

        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player.getPlayer()));
        return player;
    }
}
