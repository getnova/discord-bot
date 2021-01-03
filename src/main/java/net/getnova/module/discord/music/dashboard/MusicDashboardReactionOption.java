package net.getnova.module.discord.music.dashboard;

import discord4j.core.event.domain.message.MessageEvent;
import net.getnova.module.discord.music.GuildMusicManager;

public interface MusicDashboardReactionOption {

  void execute(MessageEvent event, GuildMusicManager musicManager);
}
