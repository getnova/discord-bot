package net.getnova.backend.discord.command;

import lombok.Getter;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.command.general.HelpCommand;
import net.getnova.backend.discord.command.general.PingCommand;
import net.getnova.backend.discord.command.music.PlayCommand;
import net.getnova.backend.discord.command.music.PlaylistCommand;
import net.getnova.backend.discord.command.music.SkipCommand;
import net.getnova.backend.discord.command.music.StopCommand;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.ServiceHandler;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

@Service(value = "discordCommand", depends = DiscordBot.class)
@Singleton
public class CommandService {

    @Getter
    private final List<Command> commands;

    @Inject
    private ServiceHandler serviceHandler;

    @Inject
    private InjectionHandler injectionHandler;

    public CommandService() {
        this.commands = new LinkedList<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        final CommandEvent commandEvent = new CommandEvent();
        this.injectionHandler.getInjector().injectMembers(commandEvent);
        this.serviceHandler.getService(DiscordBot.class).getJda().addEventListener(commandEvent);

        this.addCommand(new HelpCommand());
        this.addCommand(new PingCommand());
        this.addCommand(new PlayCommand());
        this.addCommand(new StopCommand());
        this.addCommand(new SkipCommand());
        this.addCommand(new PlaylistCommand());
    }

    Command getCommand(final String name) {
        for (final Command command : this.commands) {
            if (command.getName().equalsIgnoreCase(name)) return command;
        }
        return null;
    }

    private <T extends Command> T addCommand(final T command) {
        this.injectionHandler.getInjector().injectMembers(command);
        this.commands.add(command);
        return command;
    }
}
