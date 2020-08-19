package net.getnova.discordbot.service.command;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class CommandHandler extends ListenerAdapter {

    private static final String UNKNOWN_COMMAND_ICON = "❓";

    private final ApplicationContext context;
    private final Set<Command> commands;
    private String prefix;
    private int prefixLength;

    @Autowired
    public CommandHandler(final ApplicationContext context) {
        this.context = context;
        this.commands = new LinkedHashSet<>();
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
        this.prefixLength = prefix.length();
    }

    @Override
    public void onMessageReceived(final @NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        final String raw = event.getMessage().getContentRaw();
        int prefixLength;

        if (raw.startsWith(this.prefix)) prefixLength = this.prefixLength;
        else if (raw.startsWith("<@" + event.getJDA().getSelfUser().getId() + ">")
                || raw.startsWith("<@!" + event.getJDA().getSelfUser().getId() + ">"))
            prefixLength = raw.indexOf('>') + 1;
        else return;

        final String[] content = Arrays.stream(raw.substring(prefixLength).split("[ \t\n\r\f]"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        final String commandName = content[0].toLowerCase();
        final CommandEvent commandEvent = new CommandEvent(event);

        for (final Command command : this.commands)
            if (command.getName().equals(commandName)) {
                this.runCommand(command, commandEvent, content);
                return;
            } else if (command.getAliases() != null)
                for (final String alias : command.getAliases()) {
                    if (alias.equals(commandName)) {
                        this.runCommand(command, commandEvent, content);
                        return;
                    }
                }

        commandEvent.react(UNKNOWN_COMMAND_ICON);
    }

    private void runCommand(final Command command, final CommandEvent event, final String[] content) {
        if (command.isOnlyGuild() && event.isFromType(ChannelType.PRIVATE)) event.reactError();
        else command.execute(event, this.loadArgs(content));
    }

    private String[] loadArgs(final String[] content) {
        final String[] args = new String[content.length - 1];
        if (args.length != 0) System.arraycopy(content, 1, args, 0, content.length - 1);
        return args;
    }

    public void addCommand(final Class<? extends Command> command) {
        this.commands.add(this.context.getBean(command));
    }
}
