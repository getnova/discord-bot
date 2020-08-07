package net.getnova.backend.discord.command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode
public abstract class Command {

    private final String name;
    private final List<String> arguments;
    private final CommandCategory category;
    private final String description;

    public Command(final String name, final CommandCategory category, final String description) {
        this(name, Collections.emptyList(), category, description);
    }

    public Command(final String name, final List<String> arguments, final CommandCategory category, final String description) {
        this.name = name;
        this.arguments = arguments;
        this.category = category;
        this.description = description;
    }

    public abstract boolean checkChannel(Message message);

    public abstract void execute(Message message, String[] args);
}
