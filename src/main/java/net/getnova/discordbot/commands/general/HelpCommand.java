package net.getnova.discordbot.commands.general;

import net.getnova.discordbot.service.command.Command;
import net.getnova.discordbot.service.command.CommandEvent;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "h", false, "Shows this help.");
    }

    @Override
    public void execute(final CommandEvent event, final String[] args) {

    }
}
