package net.getnova.backend.discord.command.general;

import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

public final class GitHubCommand extends Command {

    public GitHubCommand() {
        super("github", CommandCategory.GENERAL, "Shows the link to the [GitHub Repository](https://github.com/getnova/discord-bot) of this bot.");
    }

    @Override
    public boolean checkChannel(final Message message) {
        return true;
    }

    @Override
    public void execute(final Message message, final String[] args) {
        MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createInfoEmbed("GitHub: https://github.com/getnova/discord-bot")));
    }
}
