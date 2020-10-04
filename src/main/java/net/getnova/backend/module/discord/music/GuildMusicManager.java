package net.getnova.backend.module.discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.getnova.backend.module.discord.music.dashboard.MusicDashboard;
import reactor.core.publisher.Mono;

@Slf4j
@Getter
public class GuildMusicManager {

  private final AudioPlayer player;
  private final TrackScheduler scheduler;
  @Getter(AccessLevel.NONE)
  private final D4jAudioProvider provider;
  private final MusicDashboard dashboard;

  @Setter
  private VoiceChannel voiceChannel;
  private VoiceConnection voiceConnection;

  public GuildMusicManager(final AudioPlayer player, final TextChannel textChannel) {
    this.player = player;
    this.scheduler = new TrackScheduler(this);
    this.player.addListener(this.scheduler);
    this.provider = new D4jAudioProvider(player);
    this.dashboard = new MusicDashboard(this);
    this.dashboard.initDashboard(textChannel);
  }

  public Mono<VoiceConnection> join() {
    final Mono<VoiceConnection> mono = this.voiceChannel.join(spec -> spec.setProvider(this.provider))
      .doOnNext(connection -> this.voiceConnection = connection);
    return this.voiceConnection != null ? this.leave().flatMap(ignored -> mono) : mono;
  }

  public Mono<Void> leave() {
    return this.voiceConnection.disconnect()
      .doOnNext(ignored -> this.voiceConnection = null);
  }

  public void setTextChannel(final TextChannel channel) {
    this.dashboard.initDashboard(channel);
  }
}