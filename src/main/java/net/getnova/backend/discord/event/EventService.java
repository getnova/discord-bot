package net.getnova.backend.discord.event;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Service(id = "discord-event", depends = DiscordBot.class)
@Singleton
public final class EventService {

    @Inject
    private DiscordBot bot;

    @Inject
    private InjectionHandler injectionHandler;

    public void addListener(final Class<? extends ListenerAdapter> listener) {
        this.bot.getJda().addEventListener(this.injectionHandler.getInjector().getInstance(listener));
    }
}
