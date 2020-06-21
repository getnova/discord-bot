package net.getnova.backend.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.discord.feature.music.MusicEvent;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Service(value = "discordAudio", depends = {DiscordBot.class, EventService.class})
@Singleton
@Slf4j
public final class AudioService {

    @Getter
    private final AudioPlayerManager playerManager;

    @Inject
    private EventService eventService;

    @Inject
    public AudioService(final EventService eventService) {
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.eventService.addListener(MusicEvent.class);
    }
}
