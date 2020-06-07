package net.getnova.backend.discord.command.music;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

import javax.inject.Inject;

public class SkipCommand extends Command {

    @Inject
    private AudioService audioService;

    public SkipCommand() {
        super("skip", CommandCategory.MUSIC, "Skip the current track.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        this.audioService.skip(message.getGuild());
        message.getChannel().sendMessage(Utils.createInfoEmbed("Current track skiped.")).queue();
    }
}
