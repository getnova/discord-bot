package net.getnova.backend.discord.reaction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.event.DiscordEventService;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service(id = "discord-reaction", depends = {DiscordBot.class, DiscordEventService.class})
@Singleton
@Slf4j
public final class ReactionService {

    @Getter(AccessLevel.PACKAGE)
    private final Map<Long, Consumer<GenericMessageReactionEvent>> reactionCallbacks;

    @Inject
    private DiscordEventService discordEventService;

    public ReactionService() {
        this.reactionCallbacks = new HashMap<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.discordEventService.addListener(ReactionEventListener.class);
    }

    public boolean hasReactionListener(@NotNull final Message message) {
        return this.reactionCallbacks.containsKey(message.getIdLong());
    }

    public void addReactionListener(@NotNull final Message message, @NotNull final Consumer<GenericMessageReactionEvent> consumer) {
        this.reactionCallbacks.put(message.getIdLong(), consumer);
    }

    public void removeReactionListener(@NotNull final Message message) {
        this.reactionCallbacks.remove(message.getIdLong());
    }
}
