package net.getnova.backend.discord.music.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.music.MusicService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SkipCommand extends Command {

  private final MusicService musicService;

  public SkipCommand(final MusicService musicService) {
    super("skip", "Skips the current playing track.");
    this.musicService = musicService;
  }

  @Override
  public Mono<?> execute(final String[] args, final MessageCreateEvent event) {
    return event.getGuild()
      .map(this.musicService::getMusicManager)
      .doOnNext(musicManager -> musicManager.getScheduler().nextTrack())
      .flatMap(musicService -> event.getMessage().getChannel())
      .flatMap(channel -> channel.createMessage("Skipped current track."));
  }
}
