package net.getnova.backend.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.discord.feature.music.MusicService;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service(value = "discordAudio", depends = {DiscordBot.class, EventService.class})
@Singleton
@Slf4j
public final class AudioService {

    private final Map<Long, Playlist> playlists;
    private final AudioPlayerManager playerManager;

    @Inject
    private EventService eventService;
    @Inject
    private MusicService musicService;

    @Inject
    public AudioService(final EventService eventService) {
        this.playlists = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.eventService.addListener(AudioEvent.class);
    }

    public Playlist getPlaylist(final Guild guild) {
        Playlist playlist = this.playlists.get(guild.getIdLong());

        if (playlist == null) {
            playlist = new Playlist(this.playerManager);
            this.playlists.put(guild.getIdLong(), playlist);
        }

        guild.getAudioManager().setSendingHandler(playlist.getSendHandler());
        return playlist;
    }

    public void play(final MessageChannel textChannel, final VoiceChannel voiceChannel, final String url) {
        if (!AudioUtils.isConnectedTo(voiceChannel)) AudioUtils.join(voiceChannel);

        final Playlist playlist = this.getPlaylist(voiceChannel.getGuild());
        playlist.setChannel(textChannel);

        this.playerManager.loadItemOrdered(playlist, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(final AudioTrack track) {
                playlist.add(track);
                playlist.play();
                final AudioTrackInfo info = playlist.getCurrent().getInfo();
                textChannel.sendMessage(Utils.createInfoEmbed(
                        "Now playing **" + info.title + "** from **" + info.author + "** (" + Utils.formatDuration(Duration.ofMillis(info.length)) + ")."
                )).queue();
                musicService.updateDashboard(voiceChannel.getGuild());
            }

            @Override
            public void playlistLoaded(final AudioPlaylist audioPlaylist) {
                playlist.addAll(audioPlaylist.getTracks());
                playlist.play();
                final AudioTrackInfo info = playlist.getCurrent().getInfo();
                textChannel.sendMessage(Utils.createInfoEmbed(
                        "Playlist with " + audioPlaylist.getTracks().size() + " items loaded. Now playing **" + info.title
                                + "** from **" + info.author + "** (" + Utils.formatDuration(Duration.ofMillis(info.length)) + ")."
                )).queue();
                musicService.updateDashboard(voiceChannel.getGuild());
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage(Utils.createErrorEmbed("Nothing for `" + url + "` found.")).queue();
            }

            @Override
            public void loadFailed(final FriendlyException exception) {
                textChannel.sendMessage(Utils.createErrorEmbed(
                        "Could not play `" + url + "`. Please report this error to a bot administrator so that this error can be corrected."
                )).queue();
                log.error("Unable to play " + url + ".", exception);
            }
        });
    }

    public AudioTrack skip(final Guild guild) {
        return this.getPlaylist(guild).skip();
    }

    public void stop(final Guild guild) {
        AudioUtils.stop(guild);
        this.getPlaylist(guild).clear();
    }
}
