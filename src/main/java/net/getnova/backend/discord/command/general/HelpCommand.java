package net.getnova.backend.discord.command.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.discord.command.CommandService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Collectors;

@Singleton
public final class HelpCommand extends Command {

    @Inject
    private CommandService commandService;

    public HelpCommand() {
        super("help", CommandCategory.GENERAL, "Shows this message.");
    }

    @Override
    public boolean checkChannel(final Message message) {
        return true;
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final EmbedBuilder embedBuilder = MessageUtils.createEmbedBuilder().setTitle("Help");
        for (final CommandCategory value : CommandCategory.values()) {
            embedBuilder.addField(value.getName(), this.commandService.getCommands().stream()
                    .filter(command -> command.getCategory().equals(value))
                    .map(command -> {
                        final StringBuilder builder = new StringBuilder()
                                .append("`")
                                .append(command.getName());

                        if (!command.getArguments().isEmpty()) builder.append(" ");

                        return builder.append(String.join("|", command.getArguments()))
                                .append("` ")
                                .append(command.getDescription())
                                .toString();
                    })
                    .collect(Collectors.joining("\n")), false);
        }
        MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(embedBuilder.build()));
    }
}
