package net.getnova.module.discord.music.dashboard;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.getnova.module.discord.music.GuildMusicManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class MusicDashboard {

  private static final int PAGE_SIZE = 10;

  private final GuildMusicManager musicManager;
  private final MusicDashboardService dashboardService;

  private int offset;
  private TextChannel channel;
  @Getter(AccessLevel.PACKAGE)
  private Message message;

  public MusicDashboard(final GuildMusicManager musicManager, final MusicDashboardService dashboardService) {
    this.musicManager = musicManager;
    this.dashboardService = dashboardService;
    this.offset = 0;

    // Updating the dashboard every 10 seconds and starting after 10 seconds.
    Flux.interval(Duration.ofSeconds(10), Duration.ofSeconds(10))
      .flatMap(ignored -> this.updateDashboard())
      .subscribe(); // TODO: Stop when Dashboard is destroyed: e.g. if the bot leaves a server
  }

  public void initDashboard(final TextChannel channel) {
    this.channel = channel;

    this.channel.createEmbed(this.nothingPlaying())
      .flatMap(message -> {
        this.message = message;
        this.dashboardService.createReactions(this.message);
        return this.clean();
      })
      .thenMany(this.channel.getClient().on(MessageCreateEvent.class))
      .map(MessageCreateEvent::getMessage)
      .filter(message -> message.getChannelId().equals(this.channel.getId())
        && (message.getEmbeds().size() == 0
        || message.getEmbeds().get(0).getTitle().stream().noneMatch(s -> s.startsWith("Music :small_orange_diamond: "))))
      .flatMap(message -> message.delete().delaySubscription(Duration.ofSeconds(10)))
      .subscribe();
  }

  private Mono<Void> clean() {
    if (this.channel == null || this.message == null)
      return Mono.empty();

    return this.channel.getMessagesBefore(this.message.getId())
      .transform(this.channel::bulkDeleteMessages)
      .flatMap(Message::delete)
      .then();
  }

  public void changePage(final int change) {
    this.offset += change * PAGE_SIZE;
    this.updateDashboard().subscribe();
  }

  public Mono<?> updateDashboard() {
    if (this.channel == null || this.message == null) return Mono.empty();

    return this.message.edit(spec -> spec.setEmbed(this.render()))
      .doOnError(
        cause -> {
          if (cause.getMessage() == null || !cause.getMessage().contains("Unknown Message")) {
            log.error("Unable to update dashboard.");
            return;
          }

          /* this.message was deleted */
          this.channel.createEmbed(this.render())
            .flatMap(message -> {
              this.message = message;
              this.dashboardService.createReactions(this.message);
              return this.clean();
            })
            .subscribe();
        }
      );
  }

  private Consumer<? super EmbedCreateSpec> render() {
    return (this.musicManager.getPlayer().getPlayingTrack() == null)
      ? this.nothingPlaying()
      : this.playlist();
  }

  private Consumer<? super EmbedCreateSpec> nothingPlaying() {
    return (Consumer<EmbedCreateSpec>) spec -> spec
      .setTitle("Music :small_orange_diamond: No music playback")
      .setDescription("Here you can always see the state of the music that is currently being played. Use !play and !search.")
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
        .addField("Now playing", "**[" + playingTrack.getInfo().title + "]("
          + playingTrack.getInfo().uri + ")** by " + playingTrack.getInfo().author, false)
        .addField("Progress" + (this.musicManager.getPlayer().isPaused() ? " (paused)" : ""),
          "\u25AC".repeat(Math.max(0, x - 1)) + ":white_circle:" + "\u25AC".repeat(Math.max(0, 19 - x))
            + " [" + this.formatDuration(Duration.ofMillis(playingTrack.getPosition())) + "/"
            + this.formatDuration(Duration.ofMillis(playingTrack.getDuration())) + "]", false);

      final List<AudioTrack> collect = queue.stream().limit(size).collect(Collectors.toList());
      for (int i = this.offset; i < collect.size(); i++) {
        final AudioTrackInfo info = collect.get(i).getInfo();
        spec.addField(info.author, "**" + (i + 1) + ". [" + info.title + "](" + info.uri + ")** ("
          + formatDuration(Duration.ofMillis(info.length)) + ")", false);
      }
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
