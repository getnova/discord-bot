package net.getnova.backend.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.dashboard.Dashboard;
import net.getnova.backend.discord.dashboard.DashboardService;

import javax.inject.Inject;
import java.util.Arrays;

public final class CommandEvent extends ListenerAdapter {

    @Inject
    private CommandService commandService;
    @Inject
    private DashboardService dashboardService;
    @Inject
    private DiscordBot discordBot;

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        final String message = event.getMessage().getContentRaw();

        if (message.startsWith(this.discordBot.getConfig().getPrefix()) && !(event.getAuthor().isBot() || event.getAuthor().isFake())) {
            final String[] input = Arrays.stream(message.substring(1).split("[ \t\n\r\f]")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            final Command command = this.commandService.getCommand(input[0]);

            if (command == null) {
                MessageUtils.temporallyMessage(event.getMessage(), event.getChannel().sendMessage(MessageUtils.createErrorEmbed("The command `" + input[0] + "` was not found.")));
                return;
            }

            final Class<? extends Dashboard> dashboardType = command.getDashboardType();
            if (dashboardType != null) {
                final Dashboard dashboard = this.dashboardService.getDashboard(event.getGuild(), dashboardType);
                if (dashboard == null) {
                    MessageUtils.temporallyMessage(event.getMessage(), event.getChannel().sendMessage(MessageUtils.createErrorEmbed("The command `"
                            + input[0] + "` is not configures for this server, create a text channel with the name of this module.")));
                    return;
                }

                if (!dashboard.getChannel().equals(event.getChannel())) {
                    MessageUtils.temporallyMessage(event.getMessage(), event.getChannel().sendMessage(MessageUtils.createErrorEmbed("The command `"
                            + input[0] + "`is not for this channel, try it in #" + dashboard.getId() + ".")));
                    return;
                }
            }

            command.execute(event.getMessage(), Arrays.copyOfRange(input, 1, input.length));
        }
    }
}
