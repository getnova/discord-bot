package net.getnova.discordbot.service.command;

import lombok.Getter;
import net.getnova.discordbot.commands.general.EmojiCommand;
import net.getnova.discordbot.commands.general.HelpCommand;
import net.getnova.discordbot.commands.general.PingCommand;
import net.getnova.discordbot.commands.music.PlayCommand;
import net.getnova.discordbot.commands.music.SearchCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CommandService {

    @Getter
    private final CommandHandler handler;
    @Value("${discord.prefix}")
    private String prefix;

    @Autowired
    public CommandService(final CommandHandler handler) {
        this.handler = handler;
    }

    @PostConstruct
    private void init() {
        this.handler.setPrefix(this.prefix);

        this.handler.addCommand(HelpCommand.class);
        this.handler.addCommand(PingCommand.class);
        this.handler.addCommand(EmojiCommand.class);
        this.handler.addCommand(PlayCommand.class);
        this.handler.addCommand(SearchCommand.class);
    }
}
