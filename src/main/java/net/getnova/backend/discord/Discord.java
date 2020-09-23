package net.getnova.backend.discord;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import javax.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.getnova.backend.boot.Bootstrap;
import net.getnova.backend.boot.module.Module;
import net.getnova.backend.jpa.JpaModule;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Duration;

@Slf4j
@Module(JpaModule.class)
@ComponentScan
@EnableTransactionManagement
@EnableJpaRepositories
public class Discord {

  private static final Duration LIFECYCLE_TIMEOUT = Duration.ofSeconds(10);

  @Getter
  private GatewayDiscordClient client;

  public Discord(final Bootstrap bootstrap, final DiscordConfig config) {
    if (config.getToken().isBlank()) {
      log.error("Please provide a valid Discord Bot Token. You can create or retrieve a token here: https://discord.com/developers");
      bootstrap.shutdown();
      return;
    }

    try {
      this.client = DiscordClient.create(config.getToken())
        .login().block(LIFECYCLE_TIMEOUT);
    } catch (Throwable cause) {
      if (cause.getMessage().contains("Unauthorized")) {
        log.error("Unable to connect to the Discord Api, check your token! {}", cause.getMessage());
      } else {
        log.error("Unable to connect to the Discord Api: {}", cause.getMessage(), cause);
      }
      bootstrap.shutdown();
      return;
    }

    if (this.client == null) {
      log.error("The bot was not booted correctly.");
      bootstrap.shutdown();
      return;
    }

    new Thread(this.client.onDisconnect()::block, "discord-client").start();
  }

  @PreDestroy
  private void preDestroy() {
    this.client.logout().block(LIFECYCLE_TIMEOUT);
  }
}
