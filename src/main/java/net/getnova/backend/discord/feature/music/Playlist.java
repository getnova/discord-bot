package net.getnova.backend.discord.feature.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.audio.AudioPlayerSendHandler;
import net.getnova.backend.discord.audio.AudioUtils;
import net.getnova.backend.discord.dashboard.Dashboard;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public final class Playlist extends AudioEventAdapter {

    private final AudioPlayerManager playerManager;
    @Getter
    private final AudioPlayer player;
    private final Dashboard dashboard;
    private final TextChannel channel;
    @Getter
    private final List<AudioTrack> queue;

    public Playlist(final Dashboard dashboard, final AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
        this.player = playerManager.createPlayer();
        this.player.addListener(this);
        this.dashboard = dashboard;
        this.channel = dashboard.getChannel();
        this.queue = new LinkedList<>();
    }

    public void shuffle() {
        Collections.shuffle(this.queue);
    }

    public AudioTrack getCurrent() {
        return this.queue.isEmpty() ? null : this.queue.get(0);
    }

    private void play() {
        final AudioTrack track = this.getCurrent();
        if (track != null && !this.player.startTrack(track, true)) {
            this.dashboard.update();
        }
    }

    public AudioTrack skip(final int count) {
        if (!this.queue.isEmpty() && count > 0) {
            this.queue.subList(0, Math.min(count, this.queue.size())).clear();
            this.player.stopTrack();
            this.play();
            this.dashboard.update();
        }
        return this.getCurrent();
    }

    public void stop() {
        AudioUtils.stop(this.channel.getGuild());
        this.player.stopTrack();
        this.queue.clear();
        this.dashboard.update();
    }

    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        if (endReason.equals(AudioTrackEndReason.FINISHED) || endReason.equals(AudioTrackEndReason.LOAD_FAILED))
            this.skip(1);
        if (endReason.equals(AudioTrackEndReason.LOAD_FAILED))
            MessageUtils.temporallyMessage(this.channel.sendMessage(MessageUtils.createErrorEmbed("Error while loading song.")));
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(this.player);
    }

    public void play(final VoiceChannel voiceChannel, final String url) {
        if (!AudioUtils.isConnectedTo(voiceChannel)) AudioUtils.join(voiceChannel);

        this.playerManager.loadItemOrdered(this, url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(final AudioTrack track) {
                queue.add(track);
                play();
                dashboard.update();
            }

            @Override
            public void playlistLoaded(final AudioPlaylist audioPlaylist) {
                queue.addAll(audioPlaylist.getTracks());
                shuffle();
                play();
                dashboard.update();
            }

            @Override
            public void noMatches() {
                MessageUtils.temporallyMessage(channel.sendMessage(MessageUtils.createErrorEmbed("Nothing for `" + url + "` found.")));
            }

            @Override
            public void loadFailed(final FriendlyException exception) {
                MessageUtils.temporallyMessage(channel.sendMessage(MessageUtils.createErrorEmbed(
                        "Could not play `" + url + "`. Please report this error to a bot administrator so that this error can be corrected."
                )));
                log.error("Unable to play " + url + ".", exception);
            }
        });
    }

    public AudioTrack remove(final int position) throws IndexOutOfBoundsException {
        this.queue.remove(position);
        this.dashboard.update();
        return this.getCurrent();
    }
}
