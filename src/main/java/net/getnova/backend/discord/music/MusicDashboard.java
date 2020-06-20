package net.getnova.backend.discord.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.getnova.backend.discord.dashboard.Dashboard;

public final class MusicDashboard extends Dashboard {

    public MusicDashboard(final TextChannel channel) {
        super("music", channel);
    }

    @Override
    public MessageEmbed generate() {
        return new EmbedBuilder().setTitle("test").build();
    }
}
