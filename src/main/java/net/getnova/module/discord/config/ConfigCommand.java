package net.getnova.module.discord.config;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import net.getnova.module.discord.command.Command;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ConfigCommand extends Command {

  private final ConfigService configService;

  public ConfigCommand(final ConfigService configService) {
    super("config", "Change config values.");
    this.configService = configService;
  }

  @Override
  public Mono<?> execute(final String[] args, final MessageCreateEvent event) {
    if (args.length == 1 && args[0].equalsIgnoreCase("list")) return this.list(event);
    else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) return this.reset(event, args[1]);
    else if (args.length == 3 && args[0].equalsIgnoreCase("set")) return this.set(event, args[1], args[2]);
    else
      return event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Invalid Syntax: list | reset <key> | set <key> <value>"));
  }

  private Mono<Message> list(final MessageCreateEvent event) {
    return Mono.zip(event.getGuild(), event.getMessage().getChannel())
      .flatMap(tupel -> {
        final StringBuilder builder = new StringBuilder();
        builder.append("```yaml")
          .append("\n");

        this.configService.getValues().forEach((key, value) ->
          builder.append("# ")
            .append(value.getDescription())
            .append("\n")
            .append(key)
            .append(": ")
            .append(this.configService.getValue(tupel.getT1(), key))
        );
        builder.append("```");

        return tupel.getT2().createMessage(builder.toString());
      });
  }

  private Mono<Message> reset(final MessageCreateEvent event, final String key) {
    return event.getGuild()
      .map(guild -> this.configService.setValue(guild, key, null))
      .zipWith(event.getMessage().getChannel())
      .flatMap(tuple -> {
        if (tuple.getT1()) {
          return tuple.getT2().createMessage("Value of `" + key + "` is now rested.");
        } else {
          return tuple.getT2().createMessage("Unable to find config value `" + key + "`.");
        }
      });
  }

  private Mono<Message> set(final MessageCreateEvent event, final String key, final String value) {
    return event.getGuild()
      .map(guild -> this.configService.setValue(guild, key, value))
      .zipWith(event.getMessage().getChannel())
      .flatMap(tuple -> {
        if (tuple.getT1()) {
          return tuple.getT2().createMessage("Value of `" + key + "` is now `" + value + "`.");
        } else {
          return tuple.getT2().createMessage("Unable to find config value `" + key + "`.");
        }
      });
  }
}
