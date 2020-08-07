package net.getnova.backend.discord.command;

import lombok.Getter;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.command.general.EmojiCommand;
import net.getnova.backend.discord.command.general.HelpCommand;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

@Service(id = "discord-command", depends = {DiscordBot.class, EventService.class})
@Singleton
public final class CommandService {

    @Getter
    private final List<Command> commands;

    @Inject
    private InjectionHandler injectionHandler;

    @Inject
    private EventService eventService;

    public CommandService() {
        this.commands = new LinkedList<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.eventService.addListener(CommandEvent.class);
        this.addCommand(HelpCommand.class);
        this.addCommand(EmojiCommand.class);
    }

    Command getCommand(final String name) {
        for (final Command command : this.commands) {
            if (command.getName().equalsIgnoreCase(name)) return command;
        }
        return null;
    }

    public <T extends Command> T addCommand(final Class<? extends T> clazz) {
        final T command = this.injectionHandler.getInjector().getInstance(clazz);
        this.commands.add(command);
        return command;
    }
}
