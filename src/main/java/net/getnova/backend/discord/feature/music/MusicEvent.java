package net.getnova.backend.discord.feature.music;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.discord.audio.AudioUtils;

import javax.inject.Inject;

final class MusicEvent extends ListenerAdapter {

    @Inject
    private MusicService musicService;

    @Override
    public void onGuildVoiceLeave(final GuildVoiceLeaveEvent event) {
        if (AudioUtils.isConnectedTo(event.getChannelLeft()) && event.getChannelLeft().getMembers().size() - 1 == 0)
            this.musicService.getPlaylist(event.getGuild()).stop();
    }

    @Override
    public void onGuildVoiceMove(final GuildVoiceMoveEvent event) {
        if (AudioUtils.isConnectedTo(event.getChannelLeft()) && event.getChannelLeft().getMembers().size() - 1 == 0)
            this.musicService.getPlaylist(event.getGuild()).stop();
    }
}
