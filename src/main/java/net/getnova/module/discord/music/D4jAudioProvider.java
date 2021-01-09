package net.getnova.module.discord.music;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import discord4j.voice.AudioProvider;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class D4jAudioProvider extends AudioProvider {

  private final AudioPlayer player;
  private final MutableAudioFrame frame;

  public D4jAudioProvider(final AudioPlayer player) {
    super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
    this.player = player;
    this.frame = new MutableAudioFrame();
    this.frame.setBuffer(this.getBuffer());
  }

  @Override
  public boolean provide() {
    final boolean didProvide = this.player.provide(this.frame);
    if (didProvide) {
      this.getBuffer().flip();
    }
    return didProvide;
  }
}
