package net.getnova.discordbot.service.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Command {

    private final String name;
    private final String[] aliases;
    private final String arguments;
    private final boolean onlyGuild;
    private final String description;

    public Command(final String name, final boolean onlyGuild, final String description) {
        this(name, null, null, onlyGuild, description);
    }

    public Command(final String name, final String[] aliases, final boolean onlyGuild, final String description) {
        this(name, aliases, null, onlyGuild, description);
    }

    public Command(final String name, final String arguments, final boolean onlyGuild, final String description) {
        this(name, null, arguments, onlyGuild, description);
    }

    public abstract void execute(final CommandEvent event, final String[] args);
}
