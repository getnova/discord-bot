package net.getnova.backend.discord.feature.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;

public final class SkipCommand extends Command {

    @Inject
    private AudioService audioService;
    @Inject
    private MusicService musicService;

    public SkipCommand() {
        super("skip", CommandCategory.MUSIC, MusicDashboard.class, "Skip the current track.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final AudioTrack track = this.audioService.skip(message.getGuild());
        if (track == null) {
            Utils.temporallyMessage(message.getChannel().sendMessage(Utils.createInfoEmbed("The current playlist is finished.")));
        } else this.musicService.updateDashboard(message.getGuild());
    }
}
