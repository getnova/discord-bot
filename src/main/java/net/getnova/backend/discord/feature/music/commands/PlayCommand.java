package net.getnova.backend.discord.feature.music.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicPlayer;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public final class PlayCommand extends Command {

    @Inject
    private DashboardService dashboardService;

    @Inject
    private MusicService musicService;

    public PlayCommand() {
        super("play", List.of("<url>"), CommandCategory.MUSIC, "Loads music from the given url.");
    }

    @Override
    public boolean checkChannel(final Message message) {
        final MusicDashboard dashboard = this.dashboardService.getDashboard(message.getGuild(), MusicDashboard.class);
        return dashboard != null && dashboard.getChannel() != null && dashboard.getChannel().getChannel().equals(message.getTextChannel());
    }

    @Override
    public void execute(final Message message, final String[] args) {
        if (args.length == 0) {
            message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Please provide a valid url.")).queue();
            return;
        }

        final GuildVoiceState voiceState = message.getMember().getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            message.getChannel().sendMessage(MessageUtils.createErrorEmbed("You are not connected to a voice channel.")).queue();
            return;
        }

        final MusicPlayer player = this.musicService.getPlayer(message.getGuild());

        player.setVoiceChannel(voiceState.getChannel());
        player.load(String.join(" ", args));
    }
}
