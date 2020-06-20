package net.getnova.backend.discord.feature.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.audio.Playlist;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

public final class PlaylistCommand extends Command {

    @Inject
    private AudioService audioService;

    public PlaylistCommand() {
        super("playlist", CommandCategory.MUSIC, "Shows the current playlist.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final Playlist playlist = this.audioService.getPlaylist(message.getGuild());

        final List<AudioTrack> queue = playlist.getQueue();
        if (queue.isEmpty()) {
            message.getChannel().sendMessage(Utils.createErrorEmbed("There is no music playing at the moment.")).queue();
        } else {
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
            message.getChannel().sendMessage(embedBuilder.build()).queue();
        }
    }
}
