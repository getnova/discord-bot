package net.getnova.backend.discord.command.music;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.audio.AudioUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

import javax.inject.Inject;

public final class StopCommand extends Command {

    @Inject
    private AudioService audioService;

    public StopCommand() {
        super("stop", CommandCategory.MUSIC, "Stops the current music.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        if (!AudioUtils.isConnected(message.getGuild())) {
            message.getChannel().sendMessage(Utils.createErrorEmbed("I am not connected to a voice channel.")).queue();
            return;
        }
        this.audioService.stop(message.getGuild());
        message.getChannel().sendMessage(Utils.createInfoEmbed("Stopped current playback.")).queue();
    }
}
