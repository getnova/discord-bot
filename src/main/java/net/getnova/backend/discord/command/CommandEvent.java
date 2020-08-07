package net.getnova.backend.discord.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.MessageUtils;

import javax.inject.Inject;
import java.util.Arrays;

final class CommandEvent extends ListenerAdapter {

    @Inject
    private CommandService commandService;

    @Inject
    private DiscordBot discordBot;

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        final Message message = event.getMessage();
        final String messageContent = message.getContentRaw();

        if (!event.getAuthor().isBot() && messageContent.startsWith(this.discordBot.getConfig().getPrefix())) {
            final String[] input = this.parseInput(messageContent);
            final Command command = this.commandService.getCommand(input[0]);

            if (command == null) {
                MessageUtils.temporallyMessage(message, event.getChannel().sendMessage(MessageUtils.createErrorEmbed("The command `" + input[0] + "` was not found.")));
                return;
            }

            if (command.checkChannel(message)) {
                command.execute(message, Arrays.copyOfRange(input, 1, input.length));
            }
        }
    }

    private String[] parseInput(final String message) {
        return Arrays.stream(message.substring(1).split("[ \t\n\r\f]")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }
}
