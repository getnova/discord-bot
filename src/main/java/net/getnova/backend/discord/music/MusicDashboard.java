package net.getnova.backend.discord.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MusicDashboard {

  private static final int PAGE_SIZE = 10;

  private final GuildMusicManager musicManager;

  private TextChannel channel;
  private Message message;
  private int offset;

  public MusicDashboard(final GuildMusicManager musicManager) {
    this.musicManager = musicManager;
    this.offset = 0;
  }

  public void initDashboard(final TextChannel channel) {
    this.channel = channel;

    channel.createEmbed(this.nothingPlaying())
      .flatMapMany(message -> {
        this.message = message;
        return channel.bulkDeleteMessages(channel.getMessagesBefore(message.getId()));
      })
      .flatMap(Message::delete)
      .thenMany(this.channel.getClient().on(MessageCreateEvent.class))
      .map(MessageCreateEvent::getMessage)
      .filter(message -> message.getChannelId().equals(this.channel.getId())
        && (message.getEmbeds().size() == 0
        || message.getEmbeds().get(0).getTitle().stream().noneMatch(s -> s.startsWith("Music :small_orange_diamond:"))))
      .flatMap(message -> message.delete().delaySubscription(Duration.ofSeconds(10)))
      .subscribe();


    Flux.interval(Duration.ofSeconds(10), Duration.ofSeconds(10))
      .flatMap(ignored -> this.updateDashboard())
      .subscribe();
  }

  public Mono<?> updateDashboard() {
    return this.message.edit(spec -> spec.setEmbed(
      (this.musicManager.getPlayer().getPlayingTrack() == null)
        ? this.nothingPlaying()
        : this.playlist()
    )).onErrorResume(ClientException.class, ignored -> this.channel.createEmbed(
      (this.musicManager.getPlayer().getPlayingTrack() == null)
        ? this.nothingPlaying()
        : this.playlist()
    )).doOnNext(message -> this.message = message);
  }

  private Consumer<? super EmbedCreateSpec> nothingPlaying() {
    return (Consumer<EmbedCreateSpec>) spec -> spec
      .setTitle("Music :small_orange_diamond: No music playback")
      .setDescription("Here you can always see the state of the music that is currently being played.")
      .setColor(Color.of(0x00ED9728))
      .addField("No music playback", ":x: No music is currently being played.", false);
  }

  private Consumer<? super EmbedCreateSpec> playlist() {
    final BlockingQueue<AudioTrack> queue = this.musicManager.getScheduler().getQueue();
    final AudioTrack playingTrack = this.musicManager.getPlayer().getPlayingTrack();
    int x = (int) (((double) playingTrack.getPosition() / playingTrack.getDuration()) * 20);
    final int size = Math.min(queue.size(), this.offset + PAGE_SIZE);

    return (Consumer<EmbedCreateSpec>) spec -> {
      spec.setTitle("Music :small_orange_diamond: Playlist with " + (queue.size() + 1) + " items")
        .setDescription("Here you can always see the state of the music that is currently being played.")
        .setColor(Color.of(0x00ED9728))
        .addField("Now playing", "**[" + playingTrack.getInfo().title + "](" +
          playingTrack.getInfo().uri + ")** by " + playingTrack.getInfo().author, false)
        .addField("Progress" + (this.musicManager.getPlayer().isPaused() ? " (paused)" : ""),
          "\u25AC".repeat(Math.max(0, x - 1)) + ":white_circle:" + "\u25AC".repeat(Math.max(0, 19 - x))
            + " [" + this.formatDuration(Duration.ofMillis(playingTrack.getPosition())) + "/"
            + this.formatDuration(Duration.ofMillis(playingTrack.getDuration())) + "]", false);

      final AtomicInteger i = new AtomicInteger(1);

      queue.stream().limit(size).forEach(audioTrack -> {
        final AudioTrackInfo info = audioTrack.getInfo();
        spec.addField(info.author, "**" + (i.getAndIncrement()) + ". [" + info.title + "](" + info.uri + ")** ("
          + formatDuration(Duration.ofMillis(info.length)) + ")", false);
      });
    };
  }

  public String formatDuration(final Duration duration) {
    final long seconds = duration.getSeconds();
    final long absSeconds = Math.abs(seconds);
    final String positive = String.format(
      "%02d:%02d",
      (absSeconds % 3600) / 60,
      absSeconds % 60);
    return seconds < 0 ? "-" + positive : positive;
  }
}
