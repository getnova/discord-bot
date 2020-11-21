package net.getnova.module.discord.music.dashboard;

import discord4j.core.event.domain.message.ReactionAddEvent;
import net.getnova.module.discord.music.GuildMusicManager;

public interface MusicDashboardReactionOption {

  void execute(final ReactionAddEvent event, final GuildMusicManager musicManager);
}
