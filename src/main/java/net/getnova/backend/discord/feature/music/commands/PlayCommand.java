package net.getnova.backend.discord.feature.music.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.audio.AudioUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicService;
import net.getnova.backend.discord.feature.music.Playlist;

import javax.inject.Inject;

public final class PlayCommand extends Command {

    @Inject
    private MusicService musicService;

    public PlayCommand() {
        super("play", CommandCategory.MUSIC, MusicDashboard.class, "Plays music from the given url.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final GuildVoiceState voiceState = message.getMember().getVoiceState();
        if (voiceState == null) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("You are not connected to a voice channel.")));
            return;
        }
        final Playlist playlist = this.musicService.getPlaylist(message.getGuild());
        if (args.length == 0) {
            if (playlist.getPlayer().isPaused()) {
                if (!AudioUtils.isConnectedTo(voiceState.getChannel())) AudioUtils.join(voiceState.getChannel());
                playlist.getPlayer().setPaused(false);
                return;
            } else {
                MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Please provide a valid identifier.")));
                return;
            }
        }

        playlist.play(voiceState.getChannel(), String.join(" ", args));
    }
}
