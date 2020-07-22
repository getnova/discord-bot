package net.getnova.backend.discord.dashboard.channel;

import emoji4j.Emoji;
import emoji4j.EmojiUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.dashboard.channel.reaction.DashboardReaction;
import net.getnova.backend.discord.dashboard.channel.reaction.DashboardReactionEvent;
import net.getnova.backend.discord.reaction.ReactionService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
public class DashboardChannel {

    private final ReactionService reactionService;
    private final TextChannel channel;
    private final Map<Emoji, DashboardReaction> reactions;

    private Message message;
    private boolean reactionListener = false;

    public void renderDashboard(final MessageEmbed embed, final List<DashboardReaction> reactions) {
        this.clean();
        if (this.message == null) {
            this.channel.sendMessage(embed).queue(message -> this.postRenderDashboard(message, reactions));
        } else if (!this.message.getEmbeds().get(0).equals(embed)) {
            this.message.editMessage(embed).queue(message -> this.postRenderDashboard(message, reactions));
        }
    }

    private void postRenderDashboard(final Message message, final List<DashboardReaction> reactions) {
        this.message = message;
        this.reactionService.addReactionListener(this.message, this::handleReaction);
        this.updateReactions(reactions.stream().map(DashboardReaction::getEmoji).collect(Collectors.toUnmodifiableList()));
    }

    private void handleReaction(final GenericMessageReactionEvent event) {
        final DashboardReactionEvent reactionEvent = new DashboardReactionEvent(event.getMember(), event.getGuild(), EmojiUtils.getEmoji(event.getReactionEmote().getName()));
        this.reactions.get(reactionEvent.getEmoji()).getCallback().accept(reactionEvent);
    }

    private void updateReactions(final List<Emoji> emotes) {
        this.updateRemainingReactions(emotes);
    }

    private void updateRemainingReactions(final List<Emoji> emotes) {
        if (!emotes.isEmpty()) {
            this.message.addReaction(emotes.get(0).getEmoji()).queue(ignored -> this.updateRemainingReactions(emotes.subList(1, emotes.size())));
        }
    }

    private void clean() {
        final List<Message> messages = new LinkedList<>();
        final SelfUser botUser = this.channel.getJDA().getSelfUser();

        final Message oldMessage = this.message;
        this.message = null;

        for (final Message message : this.channel.getHistory().retrievePast(50).complete()) {
            final User author = message.getAuthor();
            if (!author.equals(botUser) || !author.isBot()) messages.add(message);
            else if (this.message == null && !message.getEmbeds().isEmpty()) this.message = message;
            else messages.add(message);
        }

        if (this.message == null || this.message != oldMessage) {
            if (oldMessage != null) this.reactionService.removeReactionListener(oldMessage);
            this.reactionListener = false;
        }

        log.info(messages.toString());
        MessageUtils.delete(this.channel, messages);
    }
}
