package net.getnova.backend.discord.reaction;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.discord.MessageUtils;

import javax.inject.Inject;
import java.util.function.Consumer;

@Slf4j
class ReactionEventListener extends ListenerAdapter {

    @Inject
    private ReactionService reactionService;

    @Override
    public void onMessageReactionAdd(final MessageReactionAddEvent event) {
        this.executeEvent(event);
    }

    @Override
    public void onMessageReactionRemove(final MessageReactionRemoveEvent event) {
        this.executeEvent(event);
    }

    private void executeEvent(final GenericMessageReactionEvent event) {
        final User user = event.getUser();
        if (user != null && !user.isBot() && !user.isFake()) {
            final Consumer<ReactionEvent> reactionEventConsumer = this.reactionService.getReactionCallbacks().get(event.getMessageIdLong());

            if (reactionEventConsumer != null)
                reactionEventConsumer.accept(new ReactionEvent(MessageUtils.getEmojiName(event.getReactionEmote().getName()), event));
        }
    }
}
