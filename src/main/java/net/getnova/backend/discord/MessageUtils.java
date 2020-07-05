package net.getnova.backend.discord;

import emoji4j.Emoji;
import emoji4j.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MessageUtils {

    private static final Duration MESSAGE_DELAY = Duration.ofSeconds(20);

    private MessageUtils() {
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

    public static void temporallyMessage(final RestAction<Message> messageAction) {
        temporallyMessage(null, messageAction);
    }

    public static void temporallyMessage(final Message senderMessage, final RestAction<Message> messageAction) {
        messageAction.delay(MESSAGE_DELAY).queue(message -> {
            if (senderMessage != null) senderMessage.delete().onErrorMap(o1 -> null).queue();
            message.delete().onErrorMap(o1 -> null).queue();
        });
    }

    public static List<String> getEmojiName(final String emojiValue) {
        final Emoji emoji = EmojiUtils.getEmoji(emojiValue);
        return emoji == null ? List.of(emojiValue) : emoji.getAliases();
    }

    public static void delete(final TextChannel channel, final List<Message> messages) {
        if (messages.size() == 1) messages.get(0).delete().onErrorMap(o1 -> null).queue();
        else if (!messages.isEmpty()) {
            final Set<Message> missingMessages = new LinkedHashSet<>();
            final OffsetDateTime currentOffsetTowWeeks = OffsetDateTime.now().minusDays(12);

            final Set<Message> bulkDeleteMessages = messages.stream().filter(currentMessage -> {
                if (!currentMessage.getTimeCreated().isAfter(currentOffsetTowWeeks)) {
                    missingMessages.add(currentMessage);
                    return false;
                } else return true;
            }).collect(Collectors.toUnmodifiableSet());

            if (bulkDeleteMessages.size() > 1)
                channel.deleteMessages(bulkDeleteMessages).onErrorMap(o1 -> null).queue();
            else if (!bulkDeleteMessages.isEmpty())
                bulkDeleteMessages.iterator().next().delete().onErrorMap(o1 -> null).queue();
            missingMessages.forEach(message -> message.delete().onErrorMap(o1 -> null).queue());
        }
    }
}
