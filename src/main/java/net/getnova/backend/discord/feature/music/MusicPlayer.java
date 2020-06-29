package net.getnova.backend.discord.feature.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.getnova.backend.discord.audio.AudioUtils;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Slf4j
@EqualsAndHashCode
public final class MusicPlayer extends AudioEventAdapter implements AudioLoadResultHandler {

    @Getter
    private final Queue<AudioTrack> queue;
    private final AudioPlayerManager playerManager;
    @Getter(AccessLevel.PACKAGE)
    private final AudioPlayer player;
    private final Consumer<MusicPlayer> update;

    @Setter
    private VoiceChannel voiceChannel;
    private long pausePosition;

    MusicPlayer(final AudioPlayerManager playerManager, final Consumer<MusicPlayer> update) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.playerManager = playerManager;
        this.player = this.playerManager.createPlayer();
        this.update = update;

        this.pausePosition = Long.MIN_VALUE;
    }

    public void load(final String identifier) {
        this.playerManager.loadItem(identifier, this);
    }

    /**
     * Inserts the specified {@link AudioTrack} into this queue if it is possible to do so
     * immediately without violating capacity restrictions, throwing an
     * {@link IllegalStateException} if no space is currently available.
     *
     * @param track the {@link AudioTrack} witch should be added
     * @throws IllegalStateException if the element cannot be added at this time due to capacity restrictions
     */
    public void add(final AudioTrack track) throws IllegalStateException {
        this.queue.add(track);
        this.update.accept(this);
    }

    /**
     * Adds all of the {@link AudioTrack}s in the specified collection to this queue.
     *
     * @param tracks a collection containing {@link AudioTrack}s to be added to this collection
     * @throws IllegalStateException if not all the {@link AudioTrack}s can be added at
     *                               his time due to insertion restrictions, e.g. there is no more space
     */
    public void addAll(final Collection<AudioTrack> tracks) {
        final int oldSize = this.queue.size();
        try {
            this.queue.addAll(tracks);
            if (this.queue.size() != oldSize) this.update.accept(this);
        } catch (IllegalStateException e) {
            if (this.queue.size() != oldSize) this.update.accept(this);
            throw e;
        }
    }

    /**
     * Retrieves and removes the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    public AudioTrack peek() {
        return this.queue.peek();
    }

    public boolean play() {
        if (this.voiceChannel == null) throw new IllegalStateException("voice channel is unset");
        if (!AudioUtils.isConnectedTo(this.voiceChannel)) AudioUtils.join(this.voiceChannel);

        if (!this.queue.isEmpty()) if (this.isPaused()) {
            final AudioTrack track = this.queue.peek();
            track.setPosition(this.pausePosition);
            this.player.playTrack(this.queue.peek());
            this.pausePosition = Long.MIN_VALUE;
            this.update.accept(this);
            return true;
        } else {
            this.player.playTrack(this.queue.peek());
            this.update.accept(this);
            return true;
        }

        this.stop();
        this.update.accept(this);
        return false;
    }

    public void pause() {
        if (this.isPaused()) throw new IllegalStateException("already paused");
        if (this.voiceChannel == null) throw new IllegalStateException("voice channel is unset");
        this.pausePosition = this.player.getPlayingTrack().getPosition();
        this.player.playTrack(null);
        AudioUtils.leave(this.voiceChannel.getGuild());
        this.update.accept(this);
    }

    public boolean isPaused() {
        return this.pausePosition != Long.MIN_VALUE;
    }

    public boolean isPlaying() {
        return !this.isPaused() && this.player.getPlayingTrack() != null;
    }

    /**
     * Removes the head of this queue, and plays the new head of the queue.
     *
     * @return if the queue isn't empty, after the skipping
     */
    public boolean skip(final int count) {
        for (int i = 0; i < count; i++) this.queue.poll();
        if (!this.isPaused()) return this.play();
        this.update.accept(this);
        return !this.queue.isEmpty();
    }

    /**
     * Removes all of the {@link AudioTrack}s from this queue. And stops
     * the current music playback.
     */
    public void stop() {
        if (this.voiceChannel == null) throw new IllegalStateException("voice channel is unset");
        this.queue.clear();
        AudioUtils.leave(this.voiceChannel.getGuild());
        this.update.accept(this);
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    public long getPosition() {
        return this.isPlaying() ? this.player.getPlayingTrack().getPosition() : 0;
    }

    public long getDuration() {
        return this.isPlaying() ? this.player.getPlayingTrack().getDuration() : 0;
    }

    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) this.skip(1);
    }

    @Override
    public void onTrackException(final AudioPlayer player, final AudioTrack track, final FriendlyException exception) {
        log.error("Unable to play track \"" + track.getInfo().uri + "\".", exception);
        this.skip(1);
    }

    @Override
    public void onTrackStuck(final AudioPlayer player, final AudioTrack track, final long thresholdMs) {
        this.skip(1);
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        this.add(track);
        if (!this.isPlaying()) this.play();
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        this.addAll(playlist.getTracks());
        if (!this.isPlaying()) this.play();
    }

    @Override
    public void noMatches() {
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
    }
}
