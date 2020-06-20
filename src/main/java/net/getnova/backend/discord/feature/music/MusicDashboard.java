package net.getnova.backend.discord.feature.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.audio.Playlist;
import net.getnova.backend.discord.dashboard.Dashboard;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

public final class MusicDashboard extends Dashboard {

    @Inject
    private AudioService audioService;

    public MusicDashboard(final TextChannel channel) {
        super("music", channel);
    }

    @Override
    public MessageEmbed generate() {
        final Playlist playlist = this.audioService.getPlaylist(this.getGuild());

        final List<AudioTrack> queue = playlist.getQueue();
        if (queue.isEmpty()) return Utils.createErrorEmbed("There is no music playing at the moment.");
        else {
            final EmbedBuilder embedBuilder = Utils.createEmbedBuilder()
                    .setTitle("Playlist")
                    .setDescription("The current playlist has " + queue.size() + " items.");

            int i = 0;
            for (final AudioTrack track : queue) {
                if (i >= 10) continue;
                final AudioTrackInfo info = track.getInfo();
                embedBuilder.addField(info.author, "**" + info.title + "** (" + Utils.formatDuration(Duration.ofMillis(info.length)) + ")", false);
                i++;
            }
            return embedBuilder.build();
        }
    }
}
