package net.getnova.backend.discord;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.getnova.backend.config.ConfigValue;

@Data
@Setter(AccessLevel.NONE)
public class DiscordBotConfig {

    @ConfigValue(id = "token", comment = "Discord Api Token")
    private String token = "";
}
