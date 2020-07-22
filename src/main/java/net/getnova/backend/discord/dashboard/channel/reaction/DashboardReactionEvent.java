package net.getnova.backend.discord.dashboard.channel.reaction;

import emoji4j.Emoji;
import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

@Data
public class DashboardReactionEvent {

    private final Member member;
    private final Guild guild;
    private final Emoji emoji;
}
