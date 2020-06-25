package net.getnova.backend.discord.feature.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;

public final class SkipCommand extends Command {

    @Inject
    private MusicService musicService;

    public SkipCommand() {
        super("skip", CommandCategory.MUSIC, MusicDashboard.class, "Skip the current track or provide a number to specify how many tracks should be skipped.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        try {
            final AudioTrack track = this.musicService.getPlaylist(message.getGuild()).skip(args.length == 0 ? 1 : Integer.parseInt(args[0]));
            if (track == null)
                MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createInfoEmbed("The current playlist is finished.")));
        } catch (NumberFormatException e) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Please provide only numbers.")));
        }
    }
}
