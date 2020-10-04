package net.getnova.backend.module.discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import discord4j.voice.VoiceConnection;
import lombok.AccessLevel;
import lombok.Getter;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class TrackScheduler extends AudioEventAdapter {

  private final GuildMusicManager musicManager;
  @Getter(AccessLevel.NONE)
  private final AudioPlayer player;
  private final BlockingQueue<AudioTrack> queue;
  private AudioTrack currentTrack;
  private boolean playing;

  public TrackScheduler(final GuildMusicManager musicManager) {
    this.musicManager = musicManager;
    this.player = musicManager.getPlayer();
    this.queue = new LinkedBlockingQueue<>();
    this.currentTrack = null;
    this.playing = false;
  }

  public void queue(final AudioTrack track) {
    this.queue.offer(track);
    this.checkConnection()
      .subscribe(connection -> {
        if (!this.playing) {
          this.currentTrack = this.queue.poll();
          this.player.startTrack(this.currentTrack, true);
          this.playing = true;
        }
      });
  }

  public void queue(final AudioTrack selected, final Collection<AudioTrack> tracks) {
    tracks.forEach(this.queue::offer);
    this.checkConnection()
      .subscribe(connection -> {
        if (!this.playing) {
          if (selected != null) {
            this.currentTrack = selected;
            this.queue.remove(selected);
          } else {
            this.currentTrack = this.queue.poll();
          }
          this.player.startTrack(this.currentTrack, true);
          this.playing = true;
        }
      });
  }

  public void nextTrack() {
    // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
    // giving null to startTrack, which is a valid argument and will simply stop the player.
    this.currentTrack = this.queue.poll();
    this.player.startTrack(this.currentTrack, false);
    this.musicManager.getDashboard().updateDashboard().subscribe();

    this.playing = this.currentTrack != null;
    if (!this.playing) {
      this.musicManager.leave().subscribe();
      this.musicManager.setVoiceChannel(null);
    }
  }

  private Mono<VoiceConnection> checkConnection() {
    return this.musicManager.getVoiceChannel()
      .isMemberConnected(this.musicManager.getVoiceChannel().getClient().getSelfId())
      .flatMap(connected -> connected ? Mono.empty() : this.musicManager.join());
  }

  @Override
  public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
    // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
    if (endReason.mayStartNext) {
      this.nextTrack();
    }
  }
}
