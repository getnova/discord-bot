package net.getnova.backend.discord.feature.music.commands;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.audio.AudioUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;

public final class StopCommand extends Command {

    @Inject
    private MusicService musicService;

    public StopCommand() {
        super("leave", CommandCategory.MUSIC, MusicDashboard.class, "Stops the current music.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        if (!AudioUtils.isConnected(message.getGuild())) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("I am not connected to a voice channel.")));
            return;
        }
        this.musicService.getPlaylist(message.getGuild()).stop();
    }
}
