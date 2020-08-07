package net.getnova.backend.discord.feature.music.commands;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.dashboard.DashboardService;
import net.getnova.backend.discord.feature.music.MusicDashboard;
import net.getnova.backend.discord.feature.music.MusicService;

import javax.inject.Inject;
import java.util.List;

public final class SkipCommand extends Command {

    @Inject
    private DashboardService dashboardService;

    @Inject
    private MusicService musicService;

    public SkipCommand() {
        super("skip", List.of("[count]"), CommandCategory.MUSIC, "Skip the current or more tracks.");
    }

    @Override
    public boolean checkChannel(final Message message) {
        final MusicDashboard dashboard = this.dashboardService.getDashboard(message.getGuild(), MusicDashboard.class);
        return dashboard != null && dashboard.getChannel().getChannel().equals(message.getTextChannel());
    }

    @Override
    public void execute(final Message message, final String[] args) {
        try {
            if (!this.musicService.getPlayer(message.getGuild()).skip(args.length == 0 ? 1 : Integer.parseInt(args[0])))
                message.getChannel().sendMessage(MessageUtils.createInfoEmbed("The current playlist is finished.")).queue();
        } catch (NumberFormatException e) {
            message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Please provide only numbers.")).queue();
        }
    }
}
