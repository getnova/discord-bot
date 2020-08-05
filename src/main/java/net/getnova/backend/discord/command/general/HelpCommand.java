package net.getnova.backend.discord.command.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.command.DiscordCommandService;

import javax.inject.Inject;
import java.util.stream.Collectors;

public final class HelpCommand extends Command {

    @Inject
    private DiscordCommandService commandService;

    public HelpCommand() {
        super("help", CommandCategory.GENERAL, "Shows this message.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final EmbedBuilder embedBuilder = MessageUtils.createEmbedBuilder().setTitle("Help");
        for (final CommandCategory value : CommandCategory.values()) {
            embedBuilder.addField(value.getName(), this.commandService.getCommands().stream()
                    .filter(command -> command.getCategory().equals(value))
                    .map(command -> "**" + command.getName() + "** " + String.join("\n  ", command.getDescription()))
                    .collect(Collectors.joining("\n")), false);
        }
        MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(embedBuilder.build()));
    }
}
