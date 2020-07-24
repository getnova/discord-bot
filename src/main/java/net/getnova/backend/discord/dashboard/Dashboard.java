package net.getnova.backend.discord.dashboard;

import emoji4j.Emoji;
import emoji4j.EmojiUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.getnova.backend.discord.dashboard.channel.DashboardChannel;
import net.getnova.backend.discord.dashboard.channel.reaction.DashboardReaction;
import net.getnova.backend.discord.dashboard.channel.reaction.DashboardReactionEvent;
import net.getnova.backend.discord.reaction.ReactionService;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@EqualsAndHashCode
@Getter(AccessLevel.NONE)
public abstract class Dashboard {

    @Getter
    private final String id;
    private final List<DashboardReaction> reactions;
    private JDA jda;
    @Getter
    private Guild guild;
    @Getter
    private DashboardChannel channel;

    public Dashboard(final String id) {
        this.id = id;
        this.reactions = new LinkedList<>();
    }

    protected void addReaction(final String emote, final Function<DashboardReactionEvent, Boolean> checkAccess, final Consumer<DashboardReactionEvent> callback) {
        final Emoji emoji = EmojiUtils.getEmoji(emote);
        if (emoji == null) throw new IllegalArgumentException("Emoji \"" + emote + "\" could not be found.");
        this.reactions.add(new DashboardReaction(emoji, checkAccess, callback));
    }

    public void render() {
        this.channel.renderDashboard(this.update(), this.reactions);
    }

    protected abstract MessageEmbed update();

    void init(final ReactionService reactionService, final TextChannel channel) {
        this.jda = channel.getJDA();
        this.guild = channel.getGuild();
        this.channel = new DashboardChannel(reactionService, channel,
                this.reactions.stream().collect(Collectors.toMap(DashboardReaction::getEmoji, Function.identity())));
    }
}
