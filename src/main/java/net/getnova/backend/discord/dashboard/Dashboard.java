package net.getnova.backend.discord.dashboard;

import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.inject.Inject;
import java.util.List;

@Data
public abstract class Dashboard {

    private final String id;
    private final TextChannel channel;
    private Message message;

    @Inject
    private JDA jda;

    public final void update() {
        /* Clear all old messages. Which is not the current in "this.message". */
        final List<Message> retrievedHistory = this.channel.getHistory().retrievePast(10).complete();
        if (!retrievedHistory.isEmpty()) retrievedHistory.forEach(message -> {
            if (this.message == null && message.getAuthor().equals(this.jda.getSelfUser())) this.message = message;
            else if (this.message == null || !this.message.equals(message)) message.delete().queue();
        });

        /* Create/update the old message. */
        if (this.message == null) this.channel.sendMessage(this.generate()).queue(message -> this.message = message);
        else {
            final List<MessageEmbed> embeds = this.message.getEmbeds();
            final MessageEmbed embed = this.generate();
            if (embeds.size() == 1 && embeds.get(0).equals(embed)) this.message.editMessage(embed).queue();
        }
    }

    protected abstract MessageEmbed generate();
}
