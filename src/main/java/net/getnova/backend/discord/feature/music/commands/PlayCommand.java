package net.getnova.backend.discord.feature.music.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;

import javax.inject.Inject;

public final class PlayCommand extends Command {

    @Inject
    private AudioService audioService;

    public PlayCommand() {
        super("play", CommandCategory.MUSIC, MusicDashboard.class, "Plays music from the given url.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final GuildVoiceState voiceState = message.getMember().getVoiceState();
        if (voiceState == null) {
            Utils.temporallyMessage(message.getChannel().sendMessage(Utils.createErrorEmbed("You are not connected to a voice channel.")));
            return;
        }

        if (args.length == 0) {
            Utils.temporallyMessage(message.getChannel().sendMessage(Utils.createErrorEmbed("Please provide a valid url.")));
            return;
        }

        this.audioService.play(message.getChannel(), voiceState.getChannel(), args[0]);
    }
}
