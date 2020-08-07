package net.getnova.backend.discord.command.general;

import emoji4j.EmojiUtils;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public final class EmojiCommand extends Command {

    public EmojiCommand() {
        super("emoji", List.of("<emoji>"), CommandCategory.GENERAL, "Prints the name of a emoji.");
    }

    @Override
    public boolean checkChannel(final Message message) {
        return true;
    }

    @Override
    public void execute(final Message message, final String[] args) {
        if (args.length < 1) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Please provide a emoji.")));
            return;
        }

        MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createInfoEmbed(args[0] + " has the names `"
                + String.join(", ", EmojiUtils.getEmoji(args[0]).getAliases()) + "`.")));
    }
}
