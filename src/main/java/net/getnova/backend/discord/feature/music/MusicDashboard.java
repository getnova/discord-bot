package net.getnova.backend.discord.feature.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.dashboard.Dashboard;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Queue;

public final class MusicDashboard extends Dashboard {

    @Inject
    private MusicService musicService;

    public MusicDashboard() {
        super("music");

        this.addReactionListener("play_or_pause_button", event -> {
            final MusicPlayer player = this.musicService.getPlayer(event.getGuild());
            if (player.isPaused()) player.play();
            else player.pause();
        });
        this.addReactionListener("next_track_button", event -> this.musicService.getPlayer(event.getGuild()).skip(1));
        this.addReactionListener("stop_button", event -> this.musicService.getPlayer(event.getGuild()).stop());
    }

    @Override
    public MessageEmbed generate() {
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
        int x = (int) (((double) player.getPosition() / player.getDuration()) * 40);

        final EmbedBuilder embedBuilder = MessageUtils.createEmbedBuilder()
                .setTitle("Music :small_orange_diamond: Playlist with " + queue.size() + " items")
                .setDescription("Here you always see the state of the music that is currently being played.")
                .addField("Progress" + (player.isPaused() ? " (paused)" : ""),
                        "~~" + "-".repeat(Math.max(0, x - 1)) + ":white_circle:" + "-".repeat(Math.max(0, 39 - x)) + "~~"
                                + " (" + MessageUtils.formatDuration(Duration.ofMillis(player.getPosition())) + "/"
                                + MessageUtils.formatDuration(Duration.ofMillis(player.getDuration())) + ")", false);

        final int size = queue.size();
        int i = 0;
        for (final AudioTrack track : queue) {
            final AudioTrackInfo info = track.getInfo();
            embedBuilder.addField(info.author, "**" + (i + 1) + ". [" + info.title + "](" + info.uri + ")** ("
                    + MessageUtils.formatDuration(Duration.ofMillis(info.length)) + ")", false);
            if (i == size) break;
            i++;
        }

        return embedBuilder.build();
    }
}
