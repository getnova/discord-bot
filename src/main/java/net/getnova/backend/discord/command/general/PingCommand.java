package net.getnova.backend.discord.command.general;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", CommandCategory.GENERAL, "Ping ... Pong!");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        message.getChannel().sendMessage(Utils.createInfoEmbed("Pong!")).queue();
    }
}
