package net.getnova.discordbot.service.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AudioService extends ListenerAdapter {

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;

    public AudioService() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.playerManager);
    }

    public GuildMusicManager getMusicManager(final Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), guildId -> {
            final GuildMusicManager musicManager = new GuildMusicManager(this.playerManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
            return musicManager;
        });
    }

    public void loadAndPlay(final GuildMusicManager musicManager, final String url, final LoadResultHandler resultHandler) {
        resultHandler.setMusicManager(musicManager);
        this.playerManager.loadItemOrdered(musicManager, url, resultHandler);
    }

    @Override
    public void onGuildLeave(@NotNull final GuildLeaveEvent event) {
        this.musicManagers.remove(event.getGuild().getIdLong());
    }
}
