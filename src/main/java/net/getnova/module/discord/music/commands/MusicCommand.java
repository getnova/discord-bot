package net.getnova.module.discord.music.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import net.getnova.module.discord.command.Command;
import net.getnova.module.discord.music.GuildMusicManager;
import net.getnova.module.discord.music.MusicService;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

public abstract class MusicCommand extends Command {

  private final MusicService musicService;

  public MusicCommand(final String id, final String description, final MusicService musicService) {
    super(id, description);
    this.musicService = musicService;
  }

  protected Mono<Tuple3<Message, GuildMusicManager, VoiceChannel>> loadBase(final MessageCreateEvent event) {
    final Mono<Message> messageMono = event.getMessage().getChannel()
      .flatMap(channel -> channel.createMessage("Loading..."));
    final Mono<GuildMusicManager> guildMono = event.getGuild()
      .map(this.musicService::getMusicManager);
    final Mono<VoiceChannel> channelMono = Mono.justOrEmpty(event.getMember())
      .flatMap(Member::getVoiceState)
      .flatMap(VoiceState::getChannel);

    return Mono.zip(messageMono, guildMono, channelMono);
  }

  protected Mono<Message> checkChannel(final Message message, final GuildMusicManager musicManager,
    final VoiceChannel channel) {
    if (musicManager.getVoiceChannel() != null && !musicManager.getVoiceChannel().equals(channel)) {
      return message.edit(spec -> spec.setContent("This bot is already used in another channel."));
    }

    if (!channel.getOverwriteForMember(message.getClient().getSelfId())
      .map(permission -> !permission.getDenied().contains(Permission.CONNECT) && permission.getAllowed()
        .contains(Permission.CONNECT))
      .orElse(true)) {
      return message.edit(spec -> spec.setContent("403 Forbidden: I don't have permission to join your channel."));
    }

    return null;
  }
}
