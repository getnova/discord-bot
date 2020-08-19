package net.getnova.discordbot.commands.general;

import net.getnova.discordbot.service.command.Command;
import net.getnova.discordbot.service.command.CommandEvent;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class PingCommand extends Command {

    public PingCommand() {
        super("ping", false, "Gets the bot's latency.");
    }

    @Override
    public void execute(final CommandEvent event, final String[] args) {
        event.reply("Ping: ...", m -> {
            final long ping = event.getMessage().getTimeCreated().until(m.getTimeCreated(), ChronoUnit.MILLIS);
            m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
        });
    }
}
