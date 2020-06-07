package net.getnova.backend.discord.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Data;

@Data
public class GuildMusicManager {

    private final AudioPlayer player;
    private final AudioPlaylist scheduler;

    public GuildMusicManager(final AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new AudioPlaylist(this.player);
        this.player.addListener(this.scheduler);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(this.player);
    }
}
