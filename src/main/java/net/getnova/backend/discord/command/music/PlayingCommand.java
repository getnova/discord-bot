package net.getnova.backend.discord.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

import javax.inject.Inject;

public class PlayingCommand extends Command {

    @Inject
    private AudioService audioService;

    public PlayingCommand() {
        super("playing", CommandCategory.MUSIC, "Shows the progress of the current song.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final AudioTrack track = this.audioService.skip(message.getGuild());
        if (track == null) {
            message.getChannel().sendMessage(Utils.createInfoEmbed("No music is currently playing.")).queue();
            return;
        }

        System.out.println((track.getPosition() * 100 / track.getDuration()));
    }
}
