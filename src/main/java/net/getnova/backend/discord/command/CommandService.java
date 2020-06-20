package net.getnova.backend.discord.command;

import lombok.Getter;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.command.general.HelpCommand;
import net.getnova.backend.discord.command.general.PingCommand;
import net.getnova.backend.discord.feature.music.commands.PlayCommand;
import net.getnova.backend.discord.feature.music.commands.PlaylistCommand;
import net.getnova.backend.discord.feature.music.commands.SkipCommand;
import net.getnova.backend.discord.feature.music.commands.StopCommand;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

@Service(value = "discordCommand", depends = {DiscordBot.class, EventService.class})
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
        this.addCommand(new HelpCommand());
        this.addCommand(new PingCommand());
    }

    Command getCommand(final String name) {
        for (final Command command : this.commands) {
            if (command.getName().equalsIgnoreCase(name)) return command;
        }
        return null;
    }

    public <T extends Command> T addCommand(final T command) {
        this.injectionHandler.getInjector().injectMembers(command);
        this.commands.add(command);
        return command;
    }
}
