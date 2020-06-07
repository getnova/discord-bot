package net.getnova.backend.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.discord.Utils;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Arrays;

public class CommandEvent extends ListenerAdapter {

    @Inject
    private CommandService commandService;

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        final String message = event.getMessage().getContentRaw();

        if (!event.getAuthor().isBot() && message.startsWith("!")) {
            final String[] input = Arrays.stream(message.substring(1).split("[ \t\n\r\f]")).filter(s -> !s.isEmpty()).toArray(String[]::new);

            final Command command = this.commandService.getCommand(input[0]);
            final String[] arguments = Arrays.copyOfRange(input, 1, input.length);

            if (command == null) {
                event.getChannel().sendMessage(Utils.createErrorEmbed("The command `" + input[0] + "` was not found."))
                        .delay(Duration.ofSeconds(10))
                        .queue(msg -> {
                            msg.delete().queue();
                            event.getMessage().delete().queue();
                        });
            } else command.execute(event.getMessage(), arguments);
        }
    }
}
