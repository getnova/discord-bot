package net.getnova.backend.discord.audio;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.inject.Inject;

public final class AudioEvent extends ListenerAdapter {

    @Inject
    private AudioService audioService;

    @Override
    public void onGuildVoiceLeave(final GuildVoiceLeaveEvent event) {
        if (AudioUtils.isConnectedTo(event.getChannelLeft()) && event.getChannelLeft().getMembers().size() - 1 == 0)
            this.audioService.stop(event.getGuild());
    }

    @Override
    public void onGuildVoiceMove(final GuildVoiceMoveEvent event) {
        if (AudioUtils.isConnectedTo(event.getChannelLeft()) && event.getChannelLeft().getMembers().size() - 1 == 0)
            this.audioService.stop(event.getGuild());
    }
}
