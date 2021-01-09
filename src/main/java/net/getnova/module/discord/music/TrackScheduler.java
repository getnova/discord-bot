package net.getnova.module.discord.music;

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

  public TrackScheduler(final GuildMusicManager musicManager) {
    this.musicManager = musicManager;
    this.player = musicManager.getPlayer();
    this.queue = new LinkedBlockingQueue<>();
  }

  public void queue(final AudioTrack track) {
    if (!this.queue.offer(track)) {
      throw new IllegalStateException(String.format("Can't add track: %s (Already %s items)", track.getInfo().uri, this.queue.size()));
    }
    this.checkConnection()
      .subscribe(connection -> {
        if (!this.isPlaying()) {
          this.player.startTrack(this.cloneTrack(this.queue.poll()), true);
          this.updateDashboard();
        }
      });
  }

  public void queue(final AudioTrack selected, final Collection<AudioTrack> tracks) {
    tracks.forEach(this.queue::offer);
    this.checkConnection()
      .subscribe(connection -> {
        if (!this.isPlaying()) {
          AudioTrack track;

          if (selected != null) {
            track = selected;
            this.queue.remove(selected);
          } else {
            track = this.queue.poll();
          }
          this.player.startTrack(this.cloneTrack(track), true);
          this.updateDashboard();
        }
      });
  }

  public void nextTrack() {
    // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
    // giving null to startTrack, which is a valid argument and will simply stop the player.
    final AudioTrack track = this.queue.poll();
    this.player.startTrack(this.cloneTrack(track), false);

    this.updateDashboard();
    if (!this.isPlaying()) {
      // leave
      this.musicManager.leave().subscribe();
      this.musicManager.setVoiceChannel(null);
    }
  }

  public void stop() {
    if (this.isPlaying()) {
      this.player.stopTrack();
      this.queue.clear();
      this.updateDashboard();

      // leave
      this.musicManager.leave().subscribe();
      this.musicManager.setVoiceChannel(null);
    }
  }

  public void resume() {
    if (this.isPlaying()) throw new IllegalStateException("Currently playing");
    if (this.player.getPlayingTrack() == null) throw new IllegalStateException("No track paused");
    this.player.setPaused(false);
    this.updateDashboard();
  }

  public void pause() {
    if (!this.isPlaying()) throw new IllegalStateException("Nothing playing");
    this.player.setPaused(true);
    this.updateDashboard();
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

  private AudioTrack cloneTrack(final AudioTrack track) {
    return track == null ? null : track.makeClone();
  }

  public boolean isPlaying() {
    return this.player.getPlayingTrack() != null && !this.player.isPaused();
  }

  public boolean isPaused() {
    return this.player.isPaused();
  }

  private void updateDashboard() {
    this.musicManager.getDashboard().updateDashboard().subscribe();
  }
}
