package net.getnova.backend.discord.dashboard;

import emoji4j.EmojiUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.reaction.ReactionEvent;
import net.getnova.backend.discord.reaction.ReactionService;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
public abstract class Dashboard {

    private final String id;
    @Setter(AccessLevel.PACKAGE)
    private TextChannel channel;
    private Message message;
    @Getter(AccessLevel.NONE)
    private final Map<String, Consumer<GenericMessageReactionEvent>> reactionListeners;

    @Inject
    @Getter(AccessLevel.PROTECTED)
    private JDA jda;

    @Inject
    private ReactionService reactionService;

    protected Dashboard(final String id) {
        this.id = id;
        this.reactionListeners = new HashMap<>();
    }

    public final void update() {
        /* Clear all old messages. Which is not the current in "this.message". */
        final List<Message> messages = this.channel.getHistory().retrievePast(50).complete().stream().filter(message -> {
            final boolean messagePresent = this.message != null;
            if (!messagePresent && message.getAuthor().equals(this.jda.getSelfUser())) {
                this.message = message;
                return false;
            }
            return !messagePresent || !this.message.equals(message);
        }).collect(Collectors.toUnmodifiableList());

        messages.forEach(message -> {
            if (this.reactionService.hasReactionListener(message)) this.reactionService.removeReactionListener(message);
        });
        MessageUtils.delete(this.channel, messages);

        /* Create/update the old message. */
        if (this.message == null) this.channel.sendMessage(this.generate()).queue(message -> {
            this.message = message;
            this.updateReactions();
            this.reactionService.addReactionListener(this.message, this::handleReaction);
        });
        else {
            if (!this.reactionService.hasReactionListener(this.message))
                this.reactionService.addReactionListener(this.message, this::handleReaction);
            this.message.editMessage(this.generate()).queue();
            this.updateReactions();
        }
    }

    private void handleReaction(final ReactionEvent event) {
        Consumer<GenericMessageReactionEvent> consumer;
        for (final String emoji : event.getEmojis()) {
            consumer = this.reactionListeners.get(emoji);
            if (consumer != null) consumer.accept(event.getReactionEvent());
        }
    }

    private void updateReactions() {
        this.reactionListeners.forEach((emoji, consumer) -> this.message.addReaction(EmojiUtils.getEmoji(emoji).getEmoji()).queue());
    }

    /**
     * Generated a new {@link MessageEmbed} witch should be displayed as the dashboard.
     *
     * @return the {@link MessageEmbed}
     */
    protected abstract MessageEmbed generate();

    public final Guild getGuild() {
        return this.channel.getGuild();
    }

    protected void addReactionListener(final String emoji, final Consumer<GenericMessageReactionEvent> listener) {
        this.reactionListeners.put(emoji, listener);
    }
}
