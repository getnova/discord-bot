package net.getnova.discordbot.service.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.function.Consumer;

@Data
@Getter(AccessLevel.NONE)
public class LoadResultHandler implements AudioLoadResultHandler {

    private final Consumer<AudioTrack> trackLoaded;
    private final Consumer<AudioPlaylist> playlistLoaded;
    private final NoMatches noMatches;
    private final Consumer<FriendlyException> loadFailed;
    private GuildMusicManager musicManager;

    @Override
    public void trackLoaded(final AudioTrack track) {
        this.trackLoaded.accept(track);
        this.musicManager.getScheduler().queue(track);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        if (!playlist.isSearchResult()) this.musicManager.getScheduler().queue(playlist);
        this.playlistLoaded.accept(playlist);
    }

    @Override
    public void noMatches() {
        this.noMatches.call();
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        this.loadFailed.accept(exception);
    }

    public interface NoMatches {
        void call();
    }
}
