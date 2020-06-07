package net.getnova.backend.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Duration;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static EmbedBuilder createEmbedBuilder() {
        return new EmbedBuilder().setColor(Color.decode("#ED9728"));
    }

    public static MessageEmbed createInfoEmbed(final String message) {
        return createEmbedBuilder().addField("**Info**", message, false).build();
    }

    public static MessageEmbed createErrorEmbed(final String message) {
        return createEmbedBuilder().addField("**Error**", message, false).build();
    }

    public static String formatDuration(final Duration duration) {
        final long seconds = duration.getSeconds();
        final long absSeconds = Math.abs(seconds);
        final String positive = String.format(
                "%02d:%02d",
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
}
