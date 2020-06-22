package net.getnova.backend.discord.feature.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;

public final class RemoveCommand extends Command {

    @Inject
    private MusicService musicService;

    public RemoveCommand() {
        super("remove", CommandCategory.MUSIC, MusicDashboard.class, "Remove a track from the playlist with the given position.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        if (args.length == 0) {
            Utils.temporallyMessage(message.getChannel().sendMessage(Utils.createErrorEmbed("Please specify a position, which track should be removed.")));
            return;
        }

        try {
            final AudioTrack track = this.musicService.getPlaylist(message.getGuild()).remove(Integer.parseInt(args[0]) - 1);
            if (track == null)
                Utils.temporallyMessage(message, message.getChannel().sendMessage(Utils.createInfoEmbed("The current playlist is finished.")));
        } catch (NumberFormatException e) {
            Utils.temporallyMessage(message, message.getChannel().sendMessage(Utils.createErrorEmbed("Please provide only numbers.")));
        } catch (IndexOutOfBoundsException e) {
            Utils.temporallyMessage(message, message.getChannel().sendMessage(Utils.createErrorEmbed("Please provide a position witch is in the playlist.")));
        }
    }
}
