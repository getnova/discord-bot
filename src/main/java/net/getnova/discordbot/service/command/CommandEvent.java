package net.getnova.discordbot.service.command;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.function.Consumer;

@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CommandEvent {

    private static final String SUCCESS_ICON = "✅";
    private static final String WARNING_ICON = "⚠️";
    private static final String ERROR_ICON = "❌";

    private final MessageReceivedEvent event;

    public void reply(final String message) {
        this.getChannel().sendMessage(message).queue();
    }

    public void reply(final String message, final Consumer<Message> success) {
        this.getChannel().sendMessage(message).queue(success);
    }

    public void reply(final String message, final Consumer<Message> success, final Consumer<Throwable> failure) {
        this.getChannel().sendMessage(message).queue(success, failure);
    }

    public void reply(final MessageEmbed embed) {
        this.getChannel().sendMessage(embed).queue();
    }

    public void reply(final MessageEmbed embed, final Consumer<Message> success) {
        this.getChannel().sendMessage(embed).queue(success);
    }

    public void reply(final MessageEmbed embed, final Consumer<Message> success, final Consumer<Throwable> failure) {
        this.getChannel().sendMessage(embed).queue(success, failure);
    }

    public void reply(final Message message) {
        this.getChannel().sendMessage(message).queue();
    }

    public void reply(final Message message, final Consumer<Message> success) {
        this.getChannel().sendMessage(message).queue(success);
    }

    public void reply(final Message message, final Consumer<Message> success, final Consumer<Throwable> failure) {
        this.getChannel().sendMessage(message).queue(success, failure);
    }

    public void replyInDm(final String message) {
        if (this.isFromType(ChannelType.PRIVATE)) this.reply(message);
        else this.openPrivateChannel(channel -> channel.sendMessage(message).queue());
    }

    public void replyInDm(final String message, final Consumer<Message> success) {
        if (this.isFromType(ChannelType.PRIVATE)) this.reply(message, success);
        else this.openPrivateChannel(channel -> channel.sendMessage(message).queue(success));
    }

    public void replyInDm(final String message, final Consumer<Message> success, final Consumer<Throwable> failure) {
        if (this.isFromType(ChannelType.PRIVATE)) this.reply(message, success, failure);
        else this.openPrivateChannel(channel -> channel.sendMessage(message).queue(success, failure), failure);
    }

    public void replyInDm(final MessageEmbed embed) {
        if (this.isFromType(ChannelType.PRIVATE)) this.reply(embed);
        else this.openPrivateChannel(channel -> channel.sendMessage(embed).queue());
    }

    public void replyInDm(final MessageEmbed embed, final Consumer<Message> success) {
        if (this.isFromType(ChannelType.PRIVATE)) this.getPrivateChannel().sendMessage(embed).queue(success);
        else this.openPrivateChannel(channel -> channel.sendMessage(embed).queue(success));
    }

    public void replyInDm(final MessageEmbed embed, final Consumer<Message> success, final Consumer<Throwable> failure) {
        if (this.isFromType(ChannelType.PRIVATE)) this.getPrivateChannel().sendMessage(embed).queue(success, failure);
        else this.openPrivateChannel(channel -> channel.sendMessage(embed).queue(success, failure), failure);
    }

    public void replyInDm(final Message message) {
        if (this.isFromType(ChannelType.PRIVATE)) this.reply(message);
        else this.openPrivateChannel(channel -> channel.sendMessage(message).queue());
    }

    public void replyInDm(final Message message, final Consumer<Message> success) {
        if (this.isFromType(ChannelType.PRIVATE)) this.getPrivateChannel().sendMessage(message).queue(success);
        else this.openPrivateChannel(channel -> channel.sendMessage(message).queue(success));
    }

    public void replyInDm(final Message message, final Consumer<Message> success, final Consumer<Throwable> failure) {
        if (this.isFromType(ChannelType.PRIVATE)) this.getPrivateChannel().sendMessage(message).queue(success, failure);
        else this.openPrivateChannel(channel -> channel.sendMessage(message).queue(success, failure), failure);
    }

    public void replySuccess(final String message) {
        this.reply(SUCCESS_ICON + " " + message);
    }

    public void replySuccess(final String message, final Consumer<Message> success) {
        this.reply(SUCCESS_ICON + " " + message, success);
    }

    public void replyWarning(final String message) {
        this.reply(WARNING_ICON + " " + message);
    }

    public void replyWarning(final String message, final Consumer<Message> success) {
        this.reply(WARNING_ICON + " " + message, success);
    }

    public void replyError(final String message) {
        this.reply(ERROR_ICON + " " + message);
    }

    public void replyError(final String message, final Consumer<Message> success) {
        this.reply(ERROR_ICON + " " + message, success);
    }

    public void reactSuccess() {
        this.react(SUCCESS_ICON);
    }

    public void reactWarning() {
        this.react(WARNING_ICON);
    }

    public void reactError() {
        this.react(ERROR_ICON);
    }

    public void react(final String reaction) {
        if (reaction == null || reaction.isEmpty()) return;
        try {
            this.getMessage().addReaction(reaction.replaceAll("<a?:(.+):(\\d+)>", "$1:$2")).queue();
        } catch (PermissionException ignored) {
        }
    }

    public SelfUser getSelfUser() {
        return this.event.getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        try {
            return this.event.getGuild().getSelfMember();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public User getAuthor() {
        return this.event.getAuthor();
    }

    public MessageChannel getChannel() {
        return this.event.getChannel();
    }

    public ChannelType getChannelType() {
        return this.event.getChannelType();
    }

    public Guild getGuild() {
        return this.event.getGuild();
    }

    public JDA getJDA() {
        return this.event.getJDA();
    }

    public Member getMember() {
        return this.event.getMember();
    }

    public Message getMessage() {
        return this.event.getMessage();
    }

    public PrivateChannel getPrivateChannel() {
        return this.event.getPrivateChannel();
    }

    public TextChannel getTextChannel() {
        return this.event.getTextChannel();
    }

    public boolean isFromType(final ChannelType type) {
        return this.event.isFromType(type);
    }

    private void openPrivateChannel(final Consumer<PrivateChannel> success) {
        this.getAuthor().openPrivateChannel().queue(success);
    }

    private void openPrivateChannel(final Consumer<PrivateChannel> success, final Consumer<Throwable> falture) {
        this.getAuthor().openPrivateChannel().queue(success, falture);
    }
}
