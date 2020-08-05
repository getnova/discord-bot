package net.getnova.backend.discord.config;

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.getnova.backend.sql.SqlService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

final class ConfigListener extends ListenerAdapter {

    @Inject
    private SqlService sqlService;

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        try (Session session = this.sqlService.openSession()) {
            final Transaction transaction = session.beginTransaction();

            session.createQuery("delete from ConfigValue config where config.key.guildId = :guildId")
                    .setParameter("guildId", event.getGuild().getIdLong())
                    .executeUpdate();

            transaction.commit();
        }
    }
}
