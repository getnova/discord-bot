package net.getnova.backend.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class AudioPlaylist extends AudioEventAdapter {

    private final AudioPlayer player;
    private final List<AudioTrack> queue;

    public AudioPlaylist(final AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
    }

    public void add(final AudioTrack audioTrack) {
        this.queue.add(audioTrack);
    }

    public void addAll(final Collection<AudioTrack> audioTracks) {
        this.queue.addAll(audioTracks);
    }

    public AudioTrack getCurrent() {
        return this.queue.get(0);
    }

    public void play() {
        if (!this.player.startTrack(this.getCurrent(), true)) {
            this.queue.remove(0);
        }
    }

    public void skip() {
        if (this.queue.size() > 0) {
            this.queue.remove(0);
            this.player.startTrack(this.getCurrent(), false);
        }
    }

    public void clear() {
        this.player.stopTrack();
        this.queue.clear();
    }

    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            this.skip();
        }
    }
}
