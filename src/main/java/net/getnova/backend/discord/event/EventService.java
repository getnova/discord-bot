package net.getnova.backend.discord.event;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.StartService;
import net.getnova.backend.service.event.StartServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.Set;

@Service(value = "discord-event", depends = DiscordBot.class)
@Singleton
public final class EventService {

    private final Set<Class<? extends ListenerAdapter>> listeners;
    @Inject
    private InjectionHandler injectionHandler;

    public EventService() {
        this.listeners = new LinkedHashSet<>();
    }

    @StartService
    private void start(final StartServiceEvent event) {
        final JDA jda = this.injectionHandler.getInjector().getInstance(JDA.class);
        this.listeners.forEach(listener -> jda.addEventListener(this.injectionHandler.getInjector().getInstance(listener)));
    }

    public void addListener(final Class<? extends ListenerAdapter> listener) {
        this.listeners.add(listener);
    }
}
