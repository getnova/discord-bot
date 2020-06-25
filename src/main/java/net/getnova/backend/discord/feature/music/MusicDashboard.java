package net.getnova.backend.discord.feature.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.dashboard.Dashboard;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

public final class MusicDashboard extends Dashboard {

    @Inject
    private MusicService musicService;

    public MusicDashboard() {
        super("music");
    }

    @Override
    public MessageEmbed generate() {
        final List<AudioTrack> queue = this.musicService.getPlaylist(this.getGuild()).getQueue();
        return queue.isEmpty() ? this.nothingPlaying() : playlist(queue);
    }

    private MessageEmbed nothingPlaying() {
        return MessageUtils.createEmbedBuilder()
                .setTitle("Music :small_orange_diamond: No music playback")
                .setDescription("Here you always see the state of the music that is currently being played.")
                .addField("No music playback", ":x: No music is currently being played.", false)
                .build();
    }

    private MessageEmbed playlist(final List<AudioTrack> queue) {
        final EmbedBuilder embedBuilder = MessageUtils.createEmbedBuilder()
                .setTitle("Music :small_orange_diamond: Playlist with " + queue.size() + " items")
                .setDescription("Here you always see the state of the music that is currently being played.");

        final int size = Math.min(queue.size(), 10);
        for (int i = 0; i < size; i++) {
            final AudioTrackInfo info = queue.get(i).getInfo();
            embedBuilder.addField(info.author, "**" + (i + 1) + ". [" + info.title + "](" + info.uri + ")** (" + MessageUtils.formatDuration(Duration.ofMillis(info.length)) + ")", false);
        }
        return embedBuilder.build();
    }
}
