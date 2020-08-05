package net.getnova.backend.discord.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.getnova.backend.sql.model.TableModel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discord_config_value")
final class ConfigValue extends TableModel {

    @EmbeddedId
    private Key key;

    @Column(name = "value", nullable = false, updatable = true, length = 512)
    private String value;

    public ConfigValue(final Guild guild, final String key, final String value) {
        this.key = new Key(guild.getIdLong(), key);
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
