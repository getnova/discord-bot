package net.getnova.backend.discord.feature.music.commands;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.audio.AudioUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicService;
import net.getnova.backend.discord.feature.music.Playlist;

import javax.inject.Inject;

public final class PauseCommand extends Command {

    @Inject
    private MusicService musicService;

    public PauseCommand() {
        super("pause", CommandCategory.MUSIC, MusicDashboard.class, "Pauses the music playback.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final Playlist playlist = musicService.getPlaylist(message.getGuild());
        if (playlist.getPlayer().isPaused()) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Music is already paused!")));
        } else {
            AudioUtils.leave(message.getGuild());
            playlist.getPlayer().setPaused(true);
            playlist.getDashboard().update();
        }
    }
}
