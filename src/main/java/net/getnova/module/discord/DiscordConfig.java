package net.getnova.module.discord;

import lombok.Getter;
import net.getnova.framework.boot.config.Config;
import org.springframework.beans.factory.annotation.Value;

@Config
@Getter
public class DiscordConfig {

  @Value("${DISCORD_BOT_TOKEN:}")
  private String token;

  @Value("${DISCORD_BOT_PREFIX:!}")
  private String prefix;
}
