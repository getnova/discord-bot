package net.getnova.module.discord.config;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.getnova.framework.jpa.model.TableModel;

import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discord_config_value")
final class ConfigValue extends TableModel {

  @EmbeddedId
  private Key key;

  @Column(name = "value", nullable = false, updatable = true, length = 512)
  private String value;

  ConfigValue(final long guild, final String key, final String value) {
    this.key = new Key(guild, key);
    this.value = value;
  }

  @Data
  @Embeddable
  @NoArgsConstructor
  @AllArgsConstructor
  static final class Key implements Serializable {

    @Column(name = "guild_id", nullable = false, updatable = false)
    private long guildId;

    @Column(name = "_key", nullable = false, updatable = false, length = 32)
    private String key;
  }
}
