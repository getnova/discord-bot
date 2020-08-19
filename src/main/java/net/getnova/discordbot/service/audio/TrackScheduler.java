package net.getnova.discordbot.service.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.getnova.discordbot.utils.AudioUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final GuildMusicManager musicManager;

    public TrackScheduler(final AudioPlayer player, final GuildMusicManager musicManager) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>(200);
        this.musicManager = musicManager;
    }

    public void queue(final AudioTrack tack) {
        if (this.checkChannel() && !this.player.startTrack(tack, true)) this.queue.offer(tack);
    }

    public void queue(final AudioPlaylist playlist) {
        final List<AudioTrack> tracks = playlist.getTracks();
        if (tracks.isEmpty()) return;
        Collections.shuffle(tracks);

        final int remove = tracks.size() - this.remainingCapacity();
        if (remove > 0) tracks.subList(0, remove).clear();
        if (tracks.isEmpty()) return;

        if (this.checkChannel() && this.player.startTrack(tracks.get(0), true))
            this.queue.addAll(tracks.subList(1, tracks.size()));
        else this.queue.addAll(tracks);
    }

    public void nextTrack() {
        if (this.checkChannel()) this.player.startTrack(this.queue.poll(), false);
    }

    public int remainingCapacity() {
        return this.queue.remainingCapacity();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) this.nextTrack();
    }

    private boolean checkChannel() {
        if (this.musicManager.getChannel() == null) return false;
        if (!AudioUtils.isConnectedTo(this.musicManager.getChannel())) AudioUtils.join(this.musicManager.getChannel());
        return true;
    }
}
