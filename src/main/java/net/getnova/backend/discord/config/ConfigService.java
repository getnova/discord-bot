package net.getnova.backend.discord.config;

import discord4j.core.object.entity.Guild;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class ConfigService {

  @Getter(AccessLevel.PACKAGE)
  private final Map<String, ConfigEntry> values;
  private final ConfigValueRepository configValueRepository;

  public ConfigService(final ConfigValueRepository configValueRepository) {
    this.values = new HashMap<>();
    this.configValueRepository = configValueRepository;
  }

  public ConfigEntry addKey(final String key, final String description, final String defaultValue, final BiConsumer<Guild, String> callback) {
    final ConfigEntry entry = new ConfigEntry(description, defaultValue, callback);
    this.values.put(key, entry);
    return entry;
  }

  public boolean setValue(final Guild guild, final String key, final String value) {
    final ConfigEntry configEntry = this.values.get(key);
    if (configEntry == null) return false;

    final long guildId = guild.getId().asLong();
    final ConfigValue.Key id = new ConfigValue.Key(guildId, key);

    if (value == null || configEntry.getDefaultValue().equals(value)) {
      configEntry.getCallback().accept(guild, configEntry.getDefaultValue());
      this.configValueRepository.deleteById(id);
    } else {
      configEntry.getCallback().accept(guild, value);
      final ConfigValue configValue = this.configValueRepository.findById(id).orElse(null);
      if (configValue == null) this.configValueRepository.save(new ConfigValue(guildId, key, value));
      else {
        configValue.setValue(value);
        this.configValueRepository.save(configValue);
      }
    }

    return true;
  }

  public String getValue(final Guild guild, final String key) {
    final ConfigEntry configEntry = this.values.get(key);
    if (configEntry == null) return null;

    final ConfigValue configValue = this.configValueRepository.findById(new ConfigValue.Key(guild.getId().asLong(), key)).orElse(null);
    return configValue == null ? null : configValue.getValue() == null ? configEntry.getDefaultValue() : configValue.getValue();
  }

  @Data
  public static final class ConfigEntry {

    private final String description;
    private final String defaultValue;
    private final BiConsumer<Guild, String> callback;
  }
}
