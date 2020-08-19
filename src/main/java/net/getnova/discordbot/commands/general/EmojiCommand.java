package net.getnova.discordbot.commands.general;

import emoji4j.Emoji;
import emoji4j.EmojiUtils;
import net.getnova.discordbot.service.command.Command;
import net.getnova.discordbot.service.command.CommandEvent;
import org.springframework.stereotype.Component;

@Component
public class EmojiCommand extends Command {

    public EmojiCommand() {
        super("emoji", "<emoji>", false, "Gets the name(s) of an emoji.");
    }

    @Override
    public void execute(final CommandEvent event, final String[] args) {
        if (args.length == 0) {
            event.replyError("Please provide a emoji.");
            return;
        }

        final Emoji emoji = EmojiUtils.getEmoji(args[0]);
        if (emoji == null) {
            event.replyError("Can't find emoji `" + args[0] + "`.");
            return;
        }

        final StringBuilder builder = new StringBuilder();
        emoji.getAliases().forEach(alias -> builder.append("`")
                .append(alias)
                .append("`")
                .append(", "));

        event.reply("The name(s) of `" + args[0] + "` is/are " + builder.substring(0, builder.length() - 2) + ".");
    }
}
