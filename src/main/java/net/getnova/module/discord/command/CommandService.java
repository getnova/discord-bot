package net.getnova.module.discord.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import net.getnova.module.discord.Discord;
import net.getnova.module.discord.DiscordConfig;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommandService {

  private final Discord discord;
  private final Map<String, Command> commands;
  private final String prefix;
  private final int prefixLength;

  public CommandService(final Discord discord,
                        final Collection<Command> commands,
                        final DiscordConfig config) {

    this.discord = discord;
    this.commands = commands.stream().collect(Collectors.toMap(Command::getId, Function.identity()));
    this.prefix = config.getPrefix();
    this.prefixLength = this.prefix.length();

    this.discord.getClient().getEventDispatcher().on(MessageCreateEvent.class)
      .filter(event -> event.getMessage().getContent().startsWith(this.prefix))
      .subscribe(this::messageCreated);

    log.info("Loaded {} commands.", this.commands.size());
  }

  private void messageCreated(final MessageCreateEvent event) {
    final String rawContent = event.getMessage().getContent().substring(this.prefixLength);
    final String[] content = Arrays.stream(rawContent.split("[ \t\n\r\f]")).filter(entry -> !entry.isBlank()).toArray(String[]::new);
    final Command command = this.commands.get(content[0]);

    if (command == null) {
      event.getMessage()
        .getChannel()
        .flatMap(channel -> channel.createMessage(String.format("Command `%s` was not found.", content[0])))
        .flatMap(message -> Mono.zip(event.getMessage().delete(), message.delete()).delaySubscription(Duration.ofSeconds(10)))
        .doOnError(cause -> {
          if (cause.getMessage() == null || !cause.getMessage().contains("Unknown Message")) {
            log.error("Unable to delete \"Command not found\" message.", cause);
          }
        })
        .subscribe();
      return;
    }

    final String[] arguments = Arrays.copyOfRange(content, 1, content.length);

    try {
      command.execute(arguments, event).block();
    } catch (Throwable cause) {
      log.error("Unable to execute command \"{}\" with arguments \"{}\". ", content[0], String.join(" ", arguments),
        cause instanceof RuntimeException && cause.getCause() != null ? cause.getCause() : cause);
      event.getMessage()
        .getChannel()
        .flatMap(channel -> channel.createMessage("500 Internal Bot Error"))
        .subscribe();
    }
  }
}
