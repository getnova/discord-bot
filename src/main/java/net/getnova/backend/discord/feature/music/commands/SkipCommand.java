package net.getnova.backend.discord.feature.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;
import java.time.Duration;

public final class SkipCommand extends Command {

    @Inject
    private AudioService audioService;
    @Inject
    private MusicService musicService;

    public SkipCommand() {
        super("skip", CommandCategory.MUSIC, "Skip the current track.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final AudioTrack track = this.audioService.skip(message.getGuild());
        if (track == null) {
            message.getChannel().sendMessage(Utils.createInfoEmbed("The current playlist is finished.")).queue();
        } else {
            final AudioTrackInfo info = track.getInfo();
            message.getChannel().sendMessage(
                    Utils.createInfoEmbed("Current track skipped. Now playing **" + info.title + "** from **" + info.author
                            + "** (" + Utils.formatDuration(Duration.ofMillis(info.length)) + ")."
                    )).queue();

            musicService.updateDashboard(message.getGuild());
        }
    }
}
