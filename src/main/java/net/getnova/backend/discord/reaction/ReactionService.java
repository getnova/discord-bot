package net.getnova.backend.discord.reaction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.event.EventService;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service(value = "discord-reaction", depends = {DiscordBot.class, EventService.class})
@Singleton
@Slf4j
public final class ReactionService {

    @Getter(AccessLevel.PACKAGE)
    private final Map<Long, Consumer<ReactionEvent>> reactionCallbacks;

    @Inject
    private EventService eventService;

    public ReactionService() {
        this.reactionCallbacks = new HashMap<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.eventService.addListener(ReactionEventListener.class);
    }

    public boolean hasReactionListener(final Message message) {
        return this.reactionCallbacks.containsKey(message.getIdLong());
    }

    public void addReactionListener(final Message message, final Consumer<ReactionEvent> consumer) {
        this.reactionCallbacks.put(message.getIdLong(), consumer);
    }

    public void removeReactionListener(final Message message) {
        this.reactionCallbacks.remove(message.getIdLong());
    }
}
