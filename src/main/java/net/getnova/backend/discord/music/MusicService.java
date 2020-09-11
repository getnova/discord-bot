package net.getnova.backend.discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import discord4j.core.object.entity.Guild;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MusicService {

  private final Map<Long, GuildMusicManager> musicManagers;
  @Getter
  private final AudioPlayerManager playerManager;

  public MusicService() {
    this.musicManagers = new ConcurrentHashMap<>();
    this.playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(this.playerManager);
  }

  public synchronized GuildMusicManager getMusicManager(final Guild guild) {
    final long guildId = guild.getId().asLong();
    GuildMusicManager musicManager = this.musicManagers.get(guildId);

    if (musicManager == null) {
      musicManager = new GuildMusicManager(this.playerManager.createPlayer());
      this.musicManagers.put(guildId, musicManager);
    }

    return musicManager;
  }
}
