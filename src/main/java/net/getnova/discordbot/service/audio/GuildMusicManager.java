package net.getnova.discordbot.service.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class GuildMusicManager {

    private final AudioPlayer player;
    @Getter
    private final TrackScheduler scheduler;
    @Getter(AccessLevel.PACKAGE)
    private final AudioSendHandler sendHandler;
    @Getter
    @Setter
    private VoiceChannel channel;

    public GuildMusicManager(final AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.player, this);
        this.player.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.player);
    }
}
