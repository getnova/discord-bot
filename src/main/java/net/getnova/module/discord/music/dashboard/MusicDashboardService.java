package net.getnova.module.discord.music.dashboard;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import net.getnova.module.discord.Discord;
import net.getnova.module.discord.music.GuildMusicManager;
import net.getnova.module.discord.music.MusicService;
import net.getnova.module.discord.music.TrackScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MusicDashboardService {

  private final Discord discord;
  private final MusicService musicService;
  private final Map<String, MusicDashboardReactionOption> options;

  public MusicDashboardService(final Discord discord, final MusicService musicService) {
    this.discord = discord;
    this.musicService = musicService;

    final LinkedHashMap<String, MusicDashboardReactionOption> options = new LinkedHashMap<>();
    options
      .put("⏯️", (event, musicManager) -> this.playPause(musicManager.getScheduler(), musicManager.getDashboard()));
    options.put("⏭️", (event, musicManager) -> {
      musicManager.getScheduler().nextTrack();
      musicManager.getDashboard().updateDashboard();
    });
    options.put("⏹️", (event, musicManager) -> musicManager.getScheduler().stop());
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
      .flatMap(event -> Mono.zip(Mono.just(event), Mono.justOrEmpty(event.getGuildId())))
      .subscribe(tuple -> this.handleReaction(tuple.getT1(),
        tuple.getT1().getEmoji(), tuple.getT1().getUserId(), tuple.getT1().getMessageId(), tuple.getT2()));

    this.discord.getClient().getEventDispatcher().on(ReactionRemoveEvent.class)
      .flatMap(event -> Mono.zip(Mono.just(event), Mono.justOrEmpty(event.getGuildId())))
      .subscribe(tuple -> this.handleReaction(tuple.getT1(),
        tuple.getT1().getEmoji(), tuple.getT1().getUserId(), tuple.getT1().getMessageId(), tuple.getT2()));
  }

  private void playPause(final TrackScheduler scheduler, final MusicDashboard dashboard) {
    if (scheduler.isPlaying()) {
      scheduler.pause();
      dashboard.updateDashboard();
    } else if (scheduler.isPaused()) {
      scheduler.resume();
      dashboard.updateDashboard();
    }
  }

  private void handleReaction(final MessageEvent event, final ReactionEmoji reactionEmoji,
    final Snowflake userId, final Snowflake messageId, final Snowflake guildId) {
    if (event.getClient().getSelfId().equals(userId)) {
      return;
    }

    final GuildMusicManager musicManager = this.musicService.getMusicManager(guildId);
    if (musicManager == null) {
      return;
    }

    musicManager.getVoiceChannel()
      .getVoiceStates()
      .filter(state -> state.getUserId().equals(userId))
      .count()
      .filter(count -> count == 1)
      .subscribe(ignored -> {
        final Message message = musicManager.getDashboard().getMessage();

        if (message == null || !messageId.equals(message.getId())) {
          return;
        }

        reactionEmoji.asUnicodeEmoji()
          .flatMap(unicodeEmoji -> Optional.ofNullable(this.options.get(unicodeEmoji.getRaw())))
          .ifPresent(option -> option.execute(event, musicManager));
      });
  }
}
