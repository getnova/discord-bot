package net.getnova.backend.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.service.Service;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Service(value = "discordAudio", depends = DiscordBot.class)
@Singleton
public class AudioService {

    private final Map<String, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;

    public AudioService() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
    }

    private GuildMusicManager getGuildAudioPlayer(final Guild guild) {
        GuildMusicManager musicManager = this.musicManagers.get(guild.getId());

        if (musicManager == null) {
            musicManager = new GuildMusicManager(this.playerManager);
            this.musicManagers.put(guild.getId(), musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    public AudioTrackInfo play(final MessageChannel textChannel, final VoiceChannel voiceChannel, final String url) {
        if (!AudioUtils.isConnectedTo(voiceChannel)) AudioUtils.join(voiceChannel);
        // TODO: Java Player github examples.

        final GuildMusicManager musicManager = getGuildAudioPlayer(voiceChannel.getGuild());

        this.playerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(final AudioTrack track) {
                musicManager.getScheduler().add(track);
                play(musicManager);
            }

            @Override
            public void playlistLoaded(final AudioPlaylist playlist) {
                musicManager.getScheduler().addAll(playlist.getTracks());
                play(musicManager);
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage("Nothing found by " + url).queue();
            }

            @Override
            public void loadFailed(final FriendlyException exception) {
                textChannel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });

        return new AudioTrackInfo("TODO", "TODO", 0, "TODO", false, "TODO");
    }

    private void play(final GuildMusicManager musicManager) {
        musicManager.getScheduler().play();
    }

    public void skip(final Guild guild) {
        this.getGuildAudioPlayer(guild).getScheduler().skip();
    }

    public void stop(final Guild guild) {
        AudioUtils.stop(guild);
        this.getGuildAudioPlayer(guild).getScheduler().clear();
    }
}
