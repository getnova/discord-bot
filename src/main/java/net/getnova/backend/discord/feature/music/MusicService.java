package net.getnova.backend.discord.feature.music;

import net.dv8tion.jda.api.entities.Guild;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.CommandService;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.discord.feature.music.commands.PlayCommand;
import net.getnova.backend.discord.feature.music.commands.SkipCommand;
import net.getnova.backend.discord.feature.music.commands.StopCommand;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Service(value = "discordMusic", depends = {DiscordBot.class, CommandService.class, EventService.class, DashboardService.class, AudioService.class})
@Singleton
public class MusicService {

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
        this.playlists = new HashMap<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.commandService.addCommand(new PlayCommand());
        this.commandService.addCommand(new StopCommand());
        this.commandService.addCommand(new SkipCommand());
        this.eventService.addListener(MusicEvent.class);
        this.dashboardService.addDashboard(MusicDashboard.class);
    }

    public void updateDashboard(final Guild guild) {
        final MusicDashboard dashboard = this.dashboardService.getDashboard(guild, MusicDashboard.class);
        if (dashboard != null) dashboard.update();
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
