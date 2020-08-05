package net.getnova.backend.discord.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.getnova.backend.discord.DiscordBot;
import net.getnova.backend.discord.command.DiscordCommandService;
import net.getnova.backend.discord.event.DiscordEventService;
import net.getnova.backend.service.Service;
import net.getnova.backend.service.event.PreInitService;
import net.getnova.backend.service.event.PreInitServiceEvent;
import net.getnova.backend.sql.SqlService;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Service(id = "discord-config", depends = {DiscordBot.class, SqlService.class, DiscordEventService.class, DiscordCommandService.class})
@Singleton
public final class DiscordConfigService {

    @Getter(AccessLevel.PACKAGE)
    private final Map<String, ConfigEntry> values;

    @Inject
    private SqlService sqlService;

    @Inject
    private DiscordEventService eventService;

    @Inject
    private DiscordCommandService commandService;

    public DiscordConfigService() {
        this.values = new HashMap<>();
    }

    @PreInitService
    private void preInit(final PreInitServiceEvent event) {
        this.sqlService.addEntity(ConfigValue.class);
        this.eventService.addListener(ConfigListener.class);
        this.commandService.addCommand(new ConfigCommand());
    }

    public void addKey(final String key, final String description, final String defaultValue, final ConfigCallback callback) {
        this.values.put(key, new ConfigEntry(this.sqlService, description, defaultValue, callback));
    }

    public boolean setValue(final Guild guild, final String key, final String value) {
        final ConfigEntry configEntry = this.values.get(key);
        if (configEntry == null) return false;

        configEntry.trigger(guild, key, value);
        return true;
    }

    @Data
    static class ConfigEntry {

        private final SqlService sqlService;
        private final String description;
        private final String defaultValue;
        private final ConfigCallback callback;

        private void trigger(final Guild guild, final String key, final String value) {
            try (Session session = this.sqlService.openSession()) {
                final Transaction transaction = session.beginTransaction();

                final ConfigValue.Key configKey = new ConfigValue.Key(guild.getIdLong(), key);

                if (value == null) {
                    this.callback.call(guild, this.defaultValue);
                    session.createQuery("delete from ConfigValue config where config.key = :key")
                            .setParameter("key", configKey)
                            .executeUpdate();
                } else {
                    this.callback.call(guild, value);

                    final ConfigValue configValue = session.find(ConfigValue.class, configKey);
                    if (configValue == null) session.save(new ConfigValue(guild, key, value));
                    else {
                        configValue.setValue(value);
                        session.update(configValue);
                    }
                }
                transaction.commit();
            }
        }

        public String getValue(final Session session, final Guild guild, final String key) {
            final ConfigValue configValue = session.find(ConfigValue.class, new ConfigValue.Key(guild.getIdLong(), key));
            return configValue == null ? this.defaultValue : configValue.getValue();
        }
    }
}
