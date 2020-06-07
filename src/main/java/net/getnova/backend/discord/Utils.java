package net.getnova.backend.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

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
}
