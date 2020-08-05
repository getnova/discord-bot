package net.getnova.backend.discord.config;

import net.dv8tion.jda.api.entities.Guild;

public interface ConfigCallback {

    void call(Guild guild, String value);
}
