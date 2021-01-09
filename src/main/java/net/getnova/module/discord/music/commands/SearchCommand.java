package net.getnova.module.discord.music.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import net.getnova.module.discord.music.GuildMusicManager;
import net.getnova.module.discord.music.MusicService;
import net.getnova.module.discord.music.ResultHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SearchCommand extends MusicCommand {

  private final AudioPlayerManager playerManager;

  public SearchCommand(final MusicService musicService) {
    super("search", "Searches on YouTube for the given keyword(s).", musicService);
    this.playerManager = musicService.getPlayerManager();
  }

  @Override
  public Mono<?> execute(final String[] args, final MessageCreateEvent event) {
    if (args.length == 0) {
      return event.getMessage().getChannel()
        .flatMap(channel -> channel.createMessage("Please provide one or more keyword(s)."));
    }

    final String query = String.join(" ", args);

    return this.loadBase(event).flatMap(tuple -> {
      final GuildMusicManager musicManager = tuple.getT2();

      final Mono<Message> messageMono = this.checkChannel(tuple.getT1(), musicManager, tuple.getT3());
      if (messageMono != null) {
        return messageMono;
      }

      musicManager.setVoiceChannel(tuple.getT3());
      this.playerManager.loadItem("ytsearch:" + query, new ResultHandler(musicManager, query, tuple.getT1()));

      return Mono.empty();
    });
  }
}
