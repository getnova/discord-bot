package net.getnova.backend.module.discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ResultHandler implements AudioLoadResultHandler {

  private final GuildMusicManager musicManager;
  private final String identifier;
  private final Message message;

  @Override
  public void trackLoaded(final AudioTrack track) {
    this.musicManager.getScheduler().queue(track);
    this.message.edit(spec -> {
      final AudioTrackInfo info = track.getInfo();
      spec.setContent("Loaded **" + info.title + "** by **" + info.author + "**.");
    }).subscribe();
  }

  @Override
  public void playlistLoaded(final AudioPlaylist playlist) {
    final TrackScheduler scheduler = this.musicManager.getScheduler();
    final List<AudioTrack> tracks = playlist.getTracks();
    if (playlist.isSearchResult()) {
      final AudioTrack track = playlist.getSelectedTrack() == null ? tracks.get(0) : playlist.getSelectedTrack();
      scheduler.queue(track);
      this.message.edit(spec -> {
        final AudioTrackInfo info = track.getInfo();
        spec.setContent("Loaded **" + info.title + "** by **" + info.author + "**.");
      }).subscribe();
    } else {
      scheduler.queue(playlist.getSelectedTrack(), tracks);
      this.message.edit(spec -> spec.setContent("Loaded **" + tracks.size() + "** tracks.")).subscribe();
    }
  }

  @Override
  public void noMatches() {
    this.message.edit(spec -> spec.setContent("Nothing found for **" + this.identifier + "**.")).subscribe();
  }

  @Override
  public void loadFailed(final FriendlyException exception) {
    if (exception.getMessage().equals("Received unexpected response from YouTube.")) {
      this.message.edit(spec -> spec.setContent("Received unexpected response from YouTube. Please try again.")).subscribe();
      return;
    }

    if (exception.getMessage().equals("This video cannot be viewed anonymously.")) {
      this.message.edit(spec -> spec.setContent("This video cannot be viewed anonymously.")).subscribe();
      return;
    }

    this.message.edit(spec -> spec.setContent("Error while loading...")).subscribe();
    log.error("Unable to load tracks... ({})", this.identifier, exception);
  }
}
