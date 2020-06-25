package net.getnova.backend.discord.command.general;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

public final class PingCommand extends Command {

    public PingCommand() {
        super("ping", CommandCategory.GENERAL, "Ping ... Pong!");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createInfoEmbed("Pong!")));
    }
}
