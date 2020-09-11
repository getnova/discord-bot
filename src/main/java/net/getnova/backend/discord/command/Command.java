package net.getnova.backend.discord.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Getter
@RequiredArgsConstructor
public abstract class Command {

  private final String id;
  private final String description;

  public abstract Mono<?> execute(final String[] args, MessageCreateEvent event);
}
