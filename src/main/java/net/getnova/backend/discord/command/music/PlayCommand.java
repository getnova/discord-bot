package net.getnova.backend.discord.command.music;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

import javax.inject.Inject;

public final class PlayCommand extends Command {

    @Inject
    private AudioService audioService;

    public PlayCommand() {
        super("play", CommandCategory.MUSIC, "Plays music from the given url.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final GuildVoiceState voiceState = message.getMember().getVoiceState();
        if (voiceState == null) {
            message.getChannel().sendMessage(Utils.createErrorEmbed("You are not connected to a voice channel.")).queue();
            return;
        }

        if (args.length == 0) {
            message.getChannel().sendMessage(Utils.createErrorEmbed("Please provide a valid url.")).queue();
            return;
        }

        this.audioService.play(message.getChannel(), voiceState.getChannel(), args[0]);
    }
}
