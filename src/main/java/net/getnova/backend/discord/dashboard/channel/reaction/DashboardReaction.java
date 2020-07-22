package net.getnova.backend.discord.dashboard.channel.reaction;

import emoji4j.Emoji;
import lombok.Data;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class DashboardReaction {

    private final Emoji emoji;
    private final Function<DashboardReactionEvent, Boolean> checkAccess;
    private final Consumer<DashboardReactionEvent> callback;
}
