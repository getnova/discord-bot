package net.getnova.backend.discord.dashboard;

import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.config.ConfigService;
import net.getnova.backend.discord.reaction.ReactionService;
import net.getnova.backend.injection.InjectionHandler;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.StartService;
import net.getnova.backend.service.event.StartServiceEvent;
import net.getnova.backend.sql.SqlService;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service(id = "discord-dashboard", depends = {DiscordBot.class, ReactionService.class, ConfigService.class, SqlService.class})
@Singleton
@Slf4j
public final class DashboardService {

    private final Map<String, Class<? extends Dashboard>> dashboardTypes;
    private final Set<Dashboard> dashboards;

    @Inject
    private InjectionHandler injectionHandler;

    @Inject
    private ReactionService reactionService;

    @Inject
    private ConfigService configService;

    @Inject
    private SqlService sqlService;

    public DashboardService() {
        this.dashboardTypes = new LinkedHashMap<>();
        this.dashboards = new LinkedHashSet<>();
    }

    @StartService
    private void start(final StartServiceEvent event) {
        final Injector injector = this.injectionHandler.getInjector();
        final JDA jda = injector.getInstance(JDA.class);

        try (Session session = this.sqlService.openSession()) {
            this.dashboardTypes.forEach((id, clazz) -> {
                final String key = "channel_" + id;
                final ConfigService.ConfigEntry entry = this.configService.addKey(key,
                        "Channel for the \"" + id + "\" dashboard",
                        null,
                        (guild, value) -> this.initDashboard(jda, this.getDashboard(guild, clazz), value));

                jda.getGuilds().forEach(guild -> {
                    final Dashboard dashboard = injector.getInstance(clazz);
                    dashboard.setGuild(guild);
                    this.dashboards.add(dashboard);
                    final String value = entry.getValue(session, guild, key);
                    if (value != null) this.initDashboard(jda, dashboard, value);
                });
            });
        }
    }

    private void initDashboard(final JDA jda, final Dashboard dashboard, final String value) {
        final String channelId = value.substring(2, value.length() - 1);
        final TextChannel channel = jda.getTextChannelById(channelId);

        if (channel == null) {
            log.warn("Channel " + value + " can not be found.");
        } else {
            dashboard.init(this.reactionService, channel);
            dashboard.render();
        }
    }

    public void addDashboard(final String id, final Class<? extends Dashboard> dashboard) {
        this.dashboardTypes.put(id, dashboard);
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
