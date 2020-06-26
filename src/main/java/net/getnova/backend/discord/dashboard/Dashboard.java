package net.getnova.backend.discord.dashboard;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public abstract class Dashboard {

    private final String id;
    @Setter(AccessLevel.PACKAGE)
    private TextChannel channel;
    private Message message;

    @Inject
    @Getter(AccessLevel.PROTECTED)
    private JDA jda;

    public final void update() {
        /* Clear all old messages. Which is not the current in "this.message". */
        final List<Message> messagesToDelete = this.channel.getHistory().retrievePast(50).complete().stream().filter(message -> {
            final boolean messagePresent = this.message != null;
            if (!messagePresent && message.getAuthor().equals(this.jda.getSelfUser())) {
                this.message = message;
                return false;
            }
            return !messagePresent || !this.message.equals(message);
        }).collect(Collectors.toUnmodifiableList());
        if (messagesToDelete.size() == 1) messagesToDelete.get(0).delete().queue();
        else if (!messagesToDelete.isEmpty()) {
            final Set<Message> missingMessages = new LinkedHashSet<>();
            final OffsetDateTime currentOffsetTowWeeks = OffsetDateTime.now().minusDays(12);
            final Set<Message> bulkDeleteMessages = messagesToDelete.stream().filter(currentMessage -> {
                if (!currentMessage.getTimeCreated().isAfter(currentOffsetTowWeeks)) {
                    missingMessages.add(currentMessage);
                    return false;
                } else return true;
            }).collect(Collectors.toUnmodifiableSet());
            if (bulkDeleteMessages.size() > 1) this.channel.deleteMessages(bulkDeleteMessages).queue();
            else if (!bulkDeleteMessages.isEmpty()) bulkDeleteMessages.iterator().next().delete().queue();
            for (final Message missingMessage : missingMessages) missingMessage.delete().queue();
        }

        /* Create/update the old message. */
        if (this.message == null) this.channel.sendMessage(this.generate()).queue(message -> this.message = message);
        else {
            final List<MessageEmbed> embeds = this.message.getEmbeds();
            final MessageEmbed embed = this.generate();
            if (!(embeds.size() == 1 && embeds.get(0).equals(embed))) this.message.editMessage(embed).queue();
        }
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
}
