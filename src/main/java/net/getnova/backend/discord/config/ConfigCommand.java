package net.getnova.backend.discord.config;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.getnova.backend.discord.MessageUtils;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;
import net.getnova.backend.sql.SqlService;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
final class ConfigCommand extends Command {

    private static final MessageEmbed INVALID_ARGUMENTS = MessageUtils.createErrorEmbed("Invalid Syntax: list | reset <key> | set <key> <value>");

    @Inject
    private SqlService sqlService;

    @Inject
    private ConfigService configService;

    ConfigCommand() {
        super("config", List.of("list", "reset <key>", "set <key> <value>"), CommandCategory.ADMIN, "Update config values.");
    }

    @Override
    public boolean checkChannel(final Message message) {
        return true;
    }

    @Override
    public void execute(final Message message, final String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) this.list(message);
        else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) this.reset(message, args[1]);
        else if (args.length == 3 && args[0].equalsIgnoreCase("set")) this.set(message, args[1], args[2]);
        else MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(INVALID_ARGUMENTS));
    }

    private void list(final Message message) {
        final EmbedBuilder embedBuilder = MessageUtils.createEmbedBuilder();
        final Guild guild = message.getGuild();

        embedBuilder.setTitle("Config Values");

        try (Session session = this.sqlService.openSession()) {
            this.configService.getValues().forEach((key, value) ->
                    embedBuilder.addField(value.getDescription(), key + ": " + value.getValue(session, guild, key), false)
            );
        }

        MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(embedBuilder.build()));
    }

    private void reset(final Message message, final String key) {
        if (this.configService.setValue(message.getGuild(), key, null)) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createInfoEmbed("Value of `" + key + "` is now rested.")));
        } else {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Unable to find config value `" + key + "`.")));
        }
    }

    private void set(final Message message, final String key, final String value) {
        if (this.configService.setValue(message.getGuild(), key, value)) {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createInfoEmbed("Value of `" + key + "` is now `" + value + "`.")));
        } else {
            MessageUtils.temporallyMessage(message, message.getChannel().sendMessage(MessageUtils.createErrorEmbed("Unable to find config value `" + key + "`.")));
        }
    }
}
