package net.getnova.backend.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.getnova.backend.discord.Utils;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class Playlist extends AudioEventAdapter {

    @Getter
    private final AudioPlayer player;
    @Getter
    private final List<AudioTrack> queue;

    @Setter
    private MessageChannel channel;
    private Message message;

    public Playlist(final AudioPlayerManager playerManager) {
        this.player = playerManager.createPlayer();
        this.player.addListener(this);
        this.queue = new LinkedList<>();
    }

    public void add(final AudioTrack audioTrack) {
        this.queue.add(audioTrack);
    }

    public void addAll(final List<AudioTrack> audioTracks) {
        Collections.shuffle(audioTracks);
        this.queue.addAll(audioTracks);
    }

    public AudioTrack getCurrent() {
        return this.queue.isEmpty() ? null : this.queue.get(0);
    }

    public void play() {
        final AudioTrack track = this.getCurrent();
        if (track != null && !this.player.startTrack(track, true)) {
            this.queue.remove(0);
        }
    }

    public AudioTrack skip() {
        if (!this.queue.isEmpty()) {
            this.queue.remove(0);
            this.player.stopTrack();
            this.play();
            return this.getCurrent();
        }
        return null;
    }

    public void clear() {
        this.player.stopTrack();
        this.queue.clear();
    }

    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        if (endReason.equals(AudioTrackEndReason.FINISHED) || endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
            final AudioTrack audioTrack = this.skip();
            if (audioTrack == null) {
                this.channel.sendMessage(Utils.createInfoEmbed("The current playlist is finished.")).queue();
            } else {
                final AudioTrackInfo info = audioTrack.getInfo();
                if (message == null) {
                    this.channel.sendMessage(Utils.createInfoEmbed("Now playing **" + info.title + "** from **" + info.author
                            + "** (" + Utils.formatDuration(Duration.ofMillis(info.length)) + ").")).queue(m -> this.message = m);
                } else {
                    this.message.editMessage(Utils.createInfoEmbed("Now playing **" + info.title + "** from **" + info.author
                            + "** (" + Utils.formatDuration(Duration.ofMillis(info.length)) + ").")).queue();
                }
            }
        }
        if (endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
            this.channel.sendMessage(Utils.createErrorEmbed("Error while loading song.")).queue();
        }
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(this.player);
    }
}
