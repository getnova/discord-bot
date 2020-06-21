package net.getnova.backend.discord.command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.dashboard.Dashboard;

@Getter
@EqualsAndHashCode
public abstract class Command {

    private final String name;
    private final CommandCategory category;
    private final Class<? extends Dashboard> dashboardType;
    private final String[] description;

    public Command(final String name, final CommandCategory category, final String... description) {
        this(name, category, null, description);
    }

    public Command(final String name, final CommandCategory category, final Class<? extends Dashboard> dashboardType, final String... description) {
        this.name = name;
        this.category = category;
        this.dashboardType = dashboardType;
        this.description = description;
    }

    public abstract void execute(Message message, String[] args);
}
