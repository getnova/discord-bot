package net.getnova.backend.module.discord.music.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import net.getnova.backend.module.discord.music.GuildMusicManager;
import net.getnova.backend.module.discord.music.MusicService;
import net.getnova.backend.module.discord.music.ResultHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PlayCommand extends MusicCommand {

  private final AudioPlayerManager playerManager;

  public PlayCommand(final MusicService musicService) {
    super("play", "Plays music from the given url.", musicService);
    this.playerManager = musicService.getPlayerManager();
  }

  @Override
  public Mono<?> execute(final String[] args, final MessageCreateEvent event) {
    if (args.length == 0) {
      return event.getMessage().getChannel()
        .flatMap(channel -> channel.createMessage("Please provide a url."));
    }

    return this.loadBase(event).flatMap(tuple -> {
      final GuildMusicManager musicManager = tuple.getT2();

      final Mono<Message> messageMono = this.checkChannel(tuple.getT1(), musicManager, tuple.getT3());
      if (messageMono != null) return messageMono;

      musicManager.setVoiceChannel(tuple.getT3());
      this.playerManager.loadItem(args[0], new ResultHandler(musicManager, args[0], tuple.getT1()));

      return Mono.empty();
    });
  }
}
