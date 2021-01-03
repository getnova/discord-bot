package net.getnova.module.discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import javax.annotation.PostConstruct;
import lombok.Getter;
import net.getnova.module.discord.Discord;
import net.getnova.module.discord.music.dashboard.MusicDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MusicService {

  private final Discord discord;
  private final Map<Long, GuildMusicManager> musicManagers;
  @Getter
  private final AudioPlayerManager playerManager;

  @Lazy
  @Autowired
  private MusicDashboardService dashboardService;

  public MusicService(final Discord discord) {
    this.discord = discord;
    this.musicManagers = new ConcurrentHashMap<>();
    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(this.playerManager);
  }

  @PostConstruct
  private void postConstruct() {
    this.discord.getClient().getGuilds()
      .doOnNext(this::getMusicManager)
      .thenMany(this.discord.getClient().getEventDispatcher().on(GuildCreateEvent.class))
      .subscribe(event -> this.getMusicManager(event.getGuild()));

    // Cleanup guilds
    this.discord.getClient().on(GuildDeleteEvent.class)
      .subscribe(event -> {
        this.musicManagers.get(event.getGuildId().asLong()).dispose();
        this.musicManagers.remove(event.getGuildId().asLong());
      });

    this.discord.getClient().on(VoiceStateUpdateEvent.class)
      .map(state -> this.getMusicManager(state.getCurrent().getGuildId()))
      .filter(manager -> manager.getVoiceChannel() != null)
      .flatMap(manager -> Mono.zip(Mono.just(manager), manager.getVoiceChannel().getVoiceStates().count()))
      .subscribe(tuple -> {
        if (tuple.getT2() == 1) tuple.getT1().getScheduler().stop();
      });
  }

  public synchronized GuildMusicManager getMusicManager(final Guild guild) {
    final long guildId = guild.getId().asLong();
    GuildMusicManager musicManager = this.musicManagers.get(guildId);

    if (musicManager == null) {
      musicManager = new GuildMusicManager(
        this.playerManager.createPlayer(),
        guild.getChannels()
          .filter(channel -> channel.getName().equals("test123"))
          .filter(channel -> channel.getType().equals(Channel.Type.GUILD_TEXT))
          .cast(TextChannel.class)
          .blockFirst(),
        this.dashboardService
      );
      this.musicManagers.put(guildId, musicManager);
    }

    return musicManager;
  }

  public synchronized GuildMusicManager getMusicManager(final Snowflake guildId) {
    return this.musicManagers.get(guildId.asLong());
  }
}
