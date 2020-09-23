package net.getnova.backend.discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.Getter;
import net.getnova.backend.discord.Discord;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MusicService {

  private final Discord discord;
  private final Map<Long, GuildMusicManager> musicManagers;
  @Getter
  private final AudioPlayerManager playerManager;

  public MusicService(final Discord discord) {
    this.discord = discord;
    this.musicManagers = new ConcurrentHashMap<>();
    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(this.playerManager);

    this.discord.getClient().getGuilds()
      .subscribe(this::getMusicManager);

    this.discord.getClient().getEventDispatcher().on(GuildCreateEvent.class)
      .subscribe(event -> this.getMusicManager(event.getGuild()));
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
          .blockFirst()
      );
      this.musicManagers.put(guildId, musicManager);
    }

    return musicManager;
  }
}
