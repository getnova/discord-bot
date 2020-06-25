package net.getnova.backend.discord.dashboard;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.getnova.backend.Nova;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PostInitService;
import net.getnova.backend.service.event.PostInitServiceEvent;
import net.getnova.backend.service.event.StartService;
import net.getnova.backend.service.event.StartServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service(value = "discord-dashboard", depends = DiscordBot.class)
@Singleton
@Slf4j
public final class DashboardService {

    private final Set<Class<? extends Dashboard>> dashboardTypes;
    private final Set<Dashboard> dashboards;

    @Inject
    private Nova nova;
    @Inject
    private InjectionHandler injectionHandler;

    public DashboardService() {
        this.dashboardTypes = new LinkedHashSet<>();
        this.dashboards = new LinkedHashSet<>();
    }

    @PostInitService
    private void postInit(final PostInitServiceEvent event) {
        for (final Guild guild : this.injectionHandler.getInjector().getInstance(JDA.class).getGuilds()) {
            Dashboard dashboard;
            List<TextChannel> channels;

            for (final Class<? extends Dashboard> dashboardType : this.dashboardTypes) {
                dashboard = this.injectionHandler.getInjector().getInstance(dashboardType);
                channels = guild.getTextChannelsByName(this.nova.isDebug() ? dashboard.getId() + "-debug" : dashboard.getId(), false);
                if (!channels.isEmpty()) {
                    dashboard.setChannel(channels.get(0));
                    this.dashboards.add(dashboard);
                }
            }
        }
    }

    @StartService
    private void start(final StartServiceEvent event) {
        this.dashboards.forEach(Dashboard::update);
    }

    public void addDashboard(final Class<? extends Dashboard> dashboard) {
        this.dashboardTypes.add(dashboard);
    }

    public Set<Dashboard> getDashboards(final Class<? extends Dashboard> type) {
        return this.dashboards.stream().filter(dashboard -> type.isAssignableFrom(dashboard.getClass())).collect(Collectors.toUnmodifiableSet());
    }

    public Set<Dashboard> getDashboards(final Guild guild) {
        return this.dashboards.stream().filter(dashboard -> dashboard.getGuild().equals(guild)).collect(Collectors.toUnmodifiableSet());
    }

    public <T extends Dashboard> T getDashboard(final Guild guild, final Class<? extends T> type) {
        for (final Dashboard dashboard : this.dashboards) {
            if (dashboard.getGuild().equals(guild) && type.isAssignableFrom(dashboard.getClass())) {
                return type.cast(dashboard);
            }
        }
        return null;
    }
}
