package net.getnova.backend.discord.feature.music;

import net.dv8tion.jda.api.entities.Guild;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.CommandService;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.discord.feature.music.commands.PauseCommand;
import net.getnova.backend.discord.feature.music.commands.PlayCommand;
import net.getnova.backend.discord.feature.music.commands.RemoveCommand;
import net.getnova.backend.discord.feature.music.commands.SkipCommand;
import net.getnova.backend.discord.feature.music.commands.StopCommand;
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

@Service(value = "discord-music", depends = {DiscordBot.class, CommandService.class, EventService.class, DashboardService.class, AudioService.class})
@Singleton
public final class MusicService {

    private final Timer timer;
    private final Map<Long, Playlist> playlists;

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
        this.playlists = new HashMap<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.commandService.addCommand(new PlayCommand());
        this.commandService.addCommand(new StopCommand());
        this.commandService.addCommand(new SkipCommand());
        this.commandService.addCommand(new RemoveCommand());
        this.commandService.addCommand(new PauseCommand());
        this.eventService.addListener(MusicEvent.class);
        this.dashboardService.addDashboard(MusicDashboard.class);
    }

    @StartService
    private void start(final StartServiceEvent event) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                playlists.values().forEach((playlist) -> playlist.getDashboard().update());
            }
        }, 0, 15 * 1000);
    }

    public Playlist getPlaylist(final Guild guild) {
        Playlist playlist = this.playlists.get(guild.getIdLong());

        if (playlist == null) {
            final MusicDashboard dashboard = this.dashboardService.getDashboard(guild, MusicDashboard.class);
            if (dashboard == null) return null;
            playlist = new Playlist(dashboard, this.audioService.getPlayerManager());
            this.playlists.put(guild.getIdLong(), playlist);
        }

        guild.getAudioManager().setSendingHandler(playlist.getSendHandler());
        return playlist;
    }
}
