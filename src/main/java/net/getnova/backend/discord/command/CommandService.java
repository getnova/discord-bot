package net.getnova.backend.discord.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.getnova.backend.discord.Discord;
import net.getnova.backend.discord.DiscordConfig;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
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
                        final List<Command> commands,
                        final DiscordConfig config) {
    this.discord = discord;
    this.commands = commands.stream().collect(Collectors.toMap(Command::getId, Function.identity()));
    this.prefix = config.getPrefix();
    this.prefixLength = this.prefix.length();

    log.info("Loaded {} commands.", this.commands.size());
  }

  @PostConstruct
  private void postConstruct() {
    this.discord.getClient().getEventDispatcher().on(MessageCreateEvent.class)
      .filter(event -> event.getMessage().getContent().startsWith(this.prefix))
      .flatMap(this::messageCreated)
      .subscribe();
  }

  private Mono<?> messageCreated(final MessageCreateEvent event) {
    final String rawContent = event.getMessage().getContent().substring(this.prefixLength);
    final String[] content = Arrays.stream(rawContent.split("[ \t\n\r\f]")).filter(entry -> !entry.isBlank()).toArray(String[]::new);
    final String[] arguments = Arrays.copyOfRange(content, 1, content.length);
    final Command command = this.commands.get(content[0]);

    if (command == null)
      return event.getMessage()
        .getChannel()
        .flatMap(channel ->
          channel.createMessage(String.format("Command `%s` was not found.", content[0]))
            .doOnNext(message ->
              event.getMessage().delete().and(message.delete()).delaySubscription(Duration.ofSeconds(10)).subscribe()
            )
        );

    try {
      return command.execute(arguments, event);
    } catch (Throwable cause) {
      log.error("Unable to execute command \"{}\" with arguments \"{}\". ", content[0], String.join(" ", arguments), cause);
      return event.getMessage()
        .getChannel()
        .flatMap(channel -> channel.createMessage("500 Internal Bot Error")
          .doOnNext(message ->
            event.getMessage().delete().and(message.delete()).delaySubscription(Duration.ofSeconds(10)).subscribe()
          )
        );
    }
  }
}
