package net.getnova.backend.discord.feature.music.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicPlayer;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;

public final class LoadCommand extends Command {

    @Inject
    private MusicService musicService;

    public LoadCommand() {
        super("load", CommandCategory.MUSIC, MusicDashboard.class, "Loads music from the given url.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final GuildVoiceState voiceState = message.getMember().getVoiceState();
        if (voiceState == null) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("You are not connected to a voice channel.")));
            return;
        }

        final MusicPlayer player = this.musicService.getPlayer(message.getGuild());
        if (args.length == 0) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Please provide a valid identifier.")));
            return;
        }

        player.setVoiceChannel(voiceState.getChannel());
        player.load(String.join(" ", args));
    }
}
