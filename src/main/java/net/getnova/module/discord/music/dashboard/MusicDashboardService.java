package net.getnova.module.discord.music.dashboard;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import javax.annotation.PostConstruct;
import net.getnova.module.discord.Discord;
import net.getnova.module.discord.music.GuildMusicManager;
import net.getnova.module.discord.music.MusicService;
import net.getnova.module.discord.music.TrackScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
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

    final LinkedHashMap<String, MusicDashboardReactionOption> options = new LinkedHashMap<>();
    options.put("⏯️", (event, musicManager) -> this.playPause(musicManager.getScheduler(), musicManager.getDashboard()));
    options.put("⏹️", (event, musicManager) -> musicManager.getScheduler().stop());
    options.put("⏭️", (event, musicManager) -> {
      musicManager.getScheduler().nextTrack();
      musicManager.getDashboard().updateDashboard();
    });
    options.put("⬆️", (event, musicManager) -> musicManager.getDashboard().changePage(-1));
    options.put("⬇️", (event, musicManager) -> musicManager.getDashboard().changePage(1));
    this.options = Collections.unmodifiableMap(options);
  }

  public void createReactions(final Message message) {
    Flux.fromIterable(this.options.keySet())
      .flatMap(reaction -> message.addReaction(ReactionEmoji.unicode(reaction)))
      .subscribe();
  }

  @PostConstruct
  private void postConstruct() {
    this.discord.getClient().getEventDispatcher().on(ReactionAddEvent.class)
      .flatMap(event -> Mono.zip(Mono.just(event), event.getGuild()))
      .subscribe(tuple -> this.handleReaction(tuple.getT1(), tuple.getT2()));

    this.discord.getClient().getEventDispatcher().on(ReactionRemoveEvent.class)
      .flatMap(event -> Mono.zip(Mono.just(event), event.getGuild()))
      .subscribe(tuple -> this.handleReaction(tuple.getT1(), tuple.getT2()));
  }

  private void playPause(final TrackScheduler scheduler, final MusicDashboard dashboard) {
    if (scheduler.isPlaying()) {
      scheduler.pause();
      dashboard.updateDashboard();
    } else if (scheduler.getCurrentTrack() != null) {
      scheduler.resume();
      dashboard.updateDashboard();
    }
  }

  private void handleReaction(final ReactionAddEvent event, final Guild guild) {
    if (event.getClient().getSelfId().equals(event.getUserId())) return;

    final GuildMusicManager musicManager = this.musicService.getMusicManager(guild);
    if (musicManager == null) return;

    final Message message = musicManager.getDashboard().getMessage();

    if (message == null || !event.getMessageId().equals(message.getId())) return;

    event.getEmoji().asUnicodeEmoji()
      .flatMap(emoji -> Optional.ofNullable(this.options.get(emoji.getRaw())))
      .ifPresent(option -> option.execute(event, musicManager));
  }

  private void handleReaction(final ReactionRemoveEvent event, final Guild guild) {
    if (event.getClient().getSelfId().equals(event.getUserId())) return;

    final GuildMusicManager musicManager = this.musicService.getMusicManager(guild);
    if (musicManager == null) return;

    final Message message = musicManager.getDashboard().getMessage();

    if (message == null || !event.getMessageId().equals(message.getId())) return;

    event.getEmoji().asUnicodeEmoji()
      .flatMap(emoji -> Optional.ofNullable(this.options.get(emoji.getRaw())))
      .ifPresent(option -> option.execute(event, musicManager));
  }
}
