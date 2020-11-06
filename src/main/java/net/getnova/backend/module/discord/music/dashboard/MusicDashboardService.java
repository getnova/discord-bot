package net.getnova.backend.module.discord.music.dashboard;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import javax.annotation.PostConstruct;
import net.getnova.backend.module.discord.Discord;
import net.getnova.backend.module.discord.music.GuildMusicManager;
import net.getnova.backend.module.discord.music.MusicService;
import net.getnova.backend.module.discord.music.TrackScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Service
public class MusicDashboardService {

  private final Discord discord;
  private final MusicService musicService;
  private final Map<String, MusicDashboardReactionOption> options;

  public MusicDashboardService(final Discord discord, final MusicService musicService) {
    this.discord = discord;
    this.musicService = musicService;
    this.options = Map.of(
      "⏯️", (event, musicManager) -> this.playPause(musicManager.getScheduler()),
      "⏹️", (event, musicManager) -> musicManager.getScheduler().stop(),
      "⏭️", (event, musicManager) -> musicManager.getScheduler().nextTrack(),
      "⬆️", (event, musicManager) -> musicManager.getDashboard().changePage(1),
      "⬇️", (event, musicManager) -> musicManager.getDashboard().changePage(-1)
    );
  }

  public void createReactions(final Message message) {
    this.options.forEach((key, value) -> message.addReaction(ReactionEmoji.unicode(key)));
  }

  @PostConstruct
  private void postConstruct() {
    this.discord.getClient().getEventDispatcher().on(ReactionAddEvent.class)
      .flatMap(event -> Mono.zip(Mono.just(event), event.getGuild()))
      .subscribe(tuple -> this.handleReaction(tuple.getT1(), tuple.getT2()));
  }

  private void playPause(final TrackScheduler scheduler) {
    if (scheduler.isPlaying()) scheduler.pause();
    else scheduler.resume();
  }

  private void handleReaction(final ReactionAddEvent event, final Guild guild) {
    final GuildMusicManager musicManager = this.musicService.getMusicManager(guild);
    if (musicManager == null) return;

    final Message message = musicManager.getDashboard().getMessage();

    if (message == null || !event.getMessageId().equals(message.getId())) return;

    event.getEmoji().asUnicodeEmoji()
      .flatMap(emoji -> Optional.ofNullable(this.options.get(emoji.getRaw())))
      .ifPresent(option -> option.execute(event, musicManager));
  }
}
