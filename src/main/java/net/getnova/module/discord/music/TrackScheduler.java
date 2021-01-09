package net.getnova.module.discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import discord4j.voice.VoiceConnection;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AccessLevel;
import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * The {@link TrackScheduler} is a a manager for the {@link AudioPlayer}. It handles a {@link java.util.Queue} of {@link
 * AudioTrack}s and provide them to the {@link AudioPlayer}.
 */
@Getter
public class TrackScheduler extends AudioEventAdapter {

  private final GuildMusicManager musicManager;
  @Getter(AccessLevel.NONE)
  private final AudioPlayer player;
  private final BlockingQueue<AudioTrack> queue;

  /**
   * Create a new {@link TrackScheduler} with a {@link GuildMusicManager} to read necessary information's of the
   * corresponding {@link discord4j.core.object.entity.Guild}.
   *
   * @param musicManager the {@link GuildMusicManager} of the {@link discord4j.core.object.entity.Guild}
   */
  public TrackScheduler(final GuildMusicManager musicManager) {
    this.musicManager = musicManager;
    this.player = musicManager.getPlayer();
    this.queue = new LinkedBlockingQueue<>();
  }

  /**
   * Adds a new {@link AudioTrack} to the {@link java.util.Queue}.
   *
   * @param track a new {@link AudioTrack}
   */
  public void queue(final AudioTrack track) {
    if (!this.queue.offer(track)) {
      throw new IllegalStateException(
        String.format("Can't add track: %s (Already %s items)", track.getInfo().uri, this.queue.size()));
    }
    this.checkConnection()
      .subscribe(connection -> {
        if (!this.isPlaying()) {
          this.player.startTrack(this.cloneTrack(this.queue.poll()), true);
          this.updateDashboard();
        }
      });
  }

  /**
   * Adds a {@link Collection} of {@link AudioTrack}s to the {@link java.util.Queue}.
   *
   * @param selected a optional selected {@link AudioTrack}; e.g. from a search result
   * @param tracks   a {@link Collection} of {@link AudioTrack}s
   */
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

  /**
   * Skips the current playing {@link AudioTrack}. If the current playback if paused and will be resumed the next {@link
   * AudioTrack} starts. If the {@link java.util.Queue} is empty the {@link AudioPlayer} stops.
   */
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

  /**
   * Stops the {@link AudioPlayer} and clears the {@link java.util.Queue}.
   */
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

  /**
   * Resumed the current paused {@link AudioTrack}.
   *
   * @throws IllegalStateException if the {@link AudioPlayer} is not paused or if the is nothing currently playing.
   */
  public void resume() {
    if (this.isPlaying()) {
      throw new IllegalStateException("Currently playing");
    }

    if (this.player.getPlayingTrack() == null) {
      throw new IllegalStateException("No track paused");
    }

    this.player.setPaused(false);
    this.updateDashboard();
  }

  /**
   * Pauses the current playing {@link AudioTrack}.
   *
   * @throws IllegalStateException if the is currently nothing playing
   */
  public void pause() {
    if (!this.isPlaying()) {
      throw new IllegalStateException("Nothing playing");
    }

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

  /**
   * If the {@link AudioPlayer} plays currently something.
   *
   * @return if the {@link AudioPlayer} is currently playing something
   */
  public boolean isPlaying() {
    return this.player.getPlayingTrack() != null && !this.player.isPaused();
  }

  /**
   * If the {@link AudioPlayer} is currently paused.
   *
   * @return if the {@link AudioPlayer} is currently paused
   */
  public boolean isPaused() {
    return this.player.isPaused();
  }

  private void updateDashboard() {
    this.musicManager.getDashboard().updateDashboard().subscribe();
  }
}
