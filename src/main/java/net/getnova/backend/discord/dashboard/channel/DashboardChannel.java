package net.getnova.backend.discord.dashboard.channel;

import emoji4j.Emoji;
import emoji4j.EmojiUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.dashboard.channel.reaction.DashboardReaction;
import net.getnova.backend.discord.dashboard.channel.reaction.DashboardReactionEvent;
import net.getnova.backend.discord.reaction.ReactionService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
public final class DashboardChannel {

    private final ReactionService reactionService;
    private final TextChannel channel;
    private final Map<Emoji, DashboardReaction> reactions;

    private Message message;
    private boolean reactionListener = false;

    public void renderDashboard(final MessageEmbed embed, final List<DashboardReaction> reactions) {
        this.clean(false);
        if (this.message == null) {
            this.channel.sendMessage(embed).queue(message -> this.postRenderDashboard(message, reactions));
        } else if (this.message.getEmbeds().size() == 0) {
            this.message.delete().queue();
            this.channel.sendMessage(embed).queue(message -> this.postRenderDashboard(message, reactions));
        } else if (!this.message.getEmbeds().get(0).equals(embed)) {
            this.message.editMessage(embed).queue(message -> this.postRenderDashboard(message, reactions));
        } else {
            this.postRenderDashboard(this.message, reactions);
        }
    }

    private void postRenderDashboard(final Message message, final List<DashboardReaction> reactions) {
        this.message = message;
        this.reactionService.setReactionListener(message, this::handleReaction);
        this.updateReactions(message, reactions.stream().map(DashboardReaction::getEmoji).collect(Collectors.toUnmodifiableList()));
    }

    private void handleReaction(final GenericMessageReactionEvent event) {
        final DashboardReactionEvent reactionEvent = new DashboardReactionEvent(event.getMember(), event.getGuild(), EmojiUtils.getEmoji(event.getReactionEmote().getName()));
        final DashboardReaction dashboardReaction = this.reactions.get(reactionEvent.getEmoji());
        if (dashboardReaction != null) dashboardReaction.getCallback().accept(reactionEvent);
    }

    private void updateReactions(final Message message, final List<Emoji> emotes) {
        this.updateRemainingReactions(message, emotes);
    }

    private void updateRemainingReactions(final Message message, final List<Emoji> emotes) {
        if (!emotes.isEmpty()) {
            message.addReaction(emotes.get(0).getEmoji())
                    .queue(ignored -> this.updateRemainingReactions(message, emotes.subList(1, emotes.size())));
        }
    }

    public void clean(final boolean all) {
        final long botUserId = this.channel.getJDA().getSelfUser().getIdLong();
        final List<Message> history = this.channel.getHistory().retrievePast(50).complete();

        if (!all) if (history.contains(this.message)) history.remove(this.message);
        else {
            final Message oldMessage = this.message;
            this.message = null;

            history.removeIf(message -> {
                if (this.message == null && message.getAuthor().getIdLong() == botUserId) {
                    this.message = message;
                    return true;
                }
                return false;
            });

            if (this.message == null || this.message != oldMessage) {
                if (oldMessage != null) this.reactionService.removeReactionListener(oldMessage);
                this.reactionListener = false;
            }
        }

        MessageUtils.delete(this.channel, history);
    }
}
