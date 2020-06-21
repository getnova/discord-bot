package net.getnova.backend.discord.dashboard;

import lombok.extern.slf4j.Slf4j;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.StartService;
import net.getnova.backend.service.event.StartServiceEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.Set;

@Service(value = "discordDashboard", depends = DiscordBot.class)
@Singleton@Slf4j
public final class DashboardService {

    private final Set<Dashboard> dashboards;

    @Inject
    private InjectionHandler injectionHandler;

    public DashboardService() {
        this.dashboards = new LinkedHashSet<>();
    }

    @StartService
    private void start(final StartServiceEvent event) {
        this.dashboards.forEach(Dashboard::update);
    }

    public void addDashboard(final Dashboard dashboard) {
        this.injectionHandler.getInjector().injectMembers(dashboard);
        this.dashboards.add(dashboard);
    }
}
