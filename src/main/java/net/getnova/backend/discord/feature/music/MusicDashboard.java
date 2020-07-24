package net.getnova.backend.discord.feature.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.audio.AudioUtils;
import net.getnova.backend.discord.dashboard.Dashboard;
import net.getnova.backend.discord.dashboard.channel.reaction.DashboardReactionEvent;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Queue;

public final class MusicDashboard extends Dashboard {

    private static final int PAGE_SIZE = 10;

    @Inject
    private MusicService musicService;
    private int offset = 0;

    public MusicDashboard() {
        super("music");
        this.addReaction("play_or_pause_button", this::checkChannel, this::playOrePause);
        this.addReaction("next_track_button", this::checkChannel, this::skip);
        this.addReaction("stop_button", this::checkChannel, this::stop);
        this.addReaction("arrow_up", this::checkChannel, this::scrollUp);
        this.addReaction("arrow_down", this::checkChannel, this::scrollDown);
    }

    private boolean checkChannel(final DashboardReactionEvent event) {
        final GuildVoiceState voiceState = event.getMember().getVoiceState();
        if (voiceState == null) return false;
        final VoiceChannel channel = voiceState.getChannel();
        return channel != null && AudioUtils.isConnectedTo(channel);
    }

    private void playOrePause(final DashboardReactionEvent event) {
        final MusicPlayer player = this.musicService.getPlayer(event.getGuild());
        if (player.isPaused()) {
            final GuildVoiceState voiceState = event.getMember().getVoiceState();
            if (voiceState == null) return;
            player.play(voiceState.getChannel());
        } else if (player.isPlaying()) player.pause();
    }

    public void stop(final DashboardReactionEvent event) {
        this.musicService.getPlayer(event.getGuild()).stop();
    }

    private void skip(final DashboardReactionEvent event) {
        this.musicService.getPlayer(event.getGuild()).skip(1);
    }

    private void scrollUp(final DashboardReactionEvent event) {
        if (this.offset > 0) this.offset -= PAGE_SIZE;
        this.render();
    }

    private void scrollDown(final DashboardReactionEvent event) {
        if (this.offset < this.musicService.getPlayer(this.getGuild()).size() - PAGE_SIZE) this.offset += PAGE_SIZE;
        this.render();
    }

    @Override
    public MessageEmbed update() {
        final MusicPlayer player = this.musicService.getPlayer(this.getGuild());
        return player.isEmpty() ? this.nothingPlaying() : playlist(player);
    }

    private MessageEmbed nothingPlaying() {
        return MessageUtils.createEmbedBuilder()
                .setTitle("Music :small_orange_diamond: No music playback")
                .setDescription("Here you always see the state of the music that is currently being played.")
                .addField("No music playback", ":x: No music is currently being played.", false)
                .build();
    }

    private MessageEmbed playlist(final MusicPlayer player) {
        final Queue<AudioTrack> queue = player.getQueue();
        int x = (int) (((double) player.getPosition() / player.getDuration()) * 20);

        final EmbedBuilder embedBuilder = MessageUtils.createEmbedBuilder()
                .setTitle("Music :small_orange_diamond: Playlist with " + queue.size() + " items")
                .setDescription("Here you always see the state of the music that is currently being played.")
                .addField("Progress" + (player.isPaused() ? " (paused)" : ""),
                        "\u25AC".repeat(Math.max(0, x - 1)) + ":white_circle:" + "\u25AC".repeat(Math.max(0, 19 - x))
                                + " [" + MessageUtils.formatDuration(Duration.ofMillis(player.getPosition())) + "/"
                                + MessageUtils.formatDuration(Duration.ofMillis(player.getDuration())) + "]", false);

        final int size = Math.min(queue.size(), this.offset + PAGE_SIZE);
        final AudioTrack[] audioTracks = queue.toArray(new AudioTrack[0]);

        for (int i = this.offset; i < size; i++) {
            final AudioTrackInfo info = audioTracks[i].getInfo();
            embedBuilder.addField(info.author, "**" + (i + 1) + ". [" + info.title + "](" + info.uri + ")** ("
                    + MessageUtils.formatDuration(Duration.ofMillis(info.length)) + ")", false);
        }

        return embedBuilder.build();
    }
}
