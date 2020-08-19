package net.getnova.discordbot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public final class AudioUtils {

    private AudioUtils() {
        throw new UnsupportedOperationException();
    }

    public static void join(final VoiceChannel channel) {
        final AudioManager audioManager = channel.getGuild().getAudioManager();

        if (channel != audioManager.getConnectedChannel()) {
            if (audioManager.isConnected()) {
                audioManager.closeAudioConnection();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignored) {
                }
            }
            audioManager.openAudioConnection(channel);
        }
        audioManager.setSelfDeafened(true);
    }

    public static void leave(final Guild guild) {
        final AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected()) {
            audioManager.setSendingHandler(null);
            audioManager.closeAudioConnection();
        }
    }

    public static boolean isConnected(final Guild guild) {
        return guild.getAudioManager().isConnected();
    }

    public static boolean isConnectedTo(final VoiceChannel channel) {
        final AudioManager audioManager = channel.getGuild().getAudioManager();
        return audioManager.isConnected() && audioManager.getConnectedChannel() != null && audioManager.getConnectedChannel().equals(channel);
    }
}
