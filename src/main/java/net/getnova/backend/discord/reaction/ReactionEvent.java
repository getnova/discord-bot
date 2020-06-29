package net.getnova.backend.discord.reaction;

import lombok.Data;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.util.List;

@Data
public class ReactionEvent {

    private final List<String> emojis;
    private final GenericMessageReactionEvent reactionEvent;
}
