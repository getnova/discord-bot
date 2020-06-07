package net.getnova.backend.discord.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.getnova.backend.discord.Utils;
import net.getnova.backend.discord.audio.AudioService;
import net.getnova.backend.discord.command.Command;
import net.getnova.backend.discord.command.CommandCategory;

import javax.inject.Inject;

public class PlayCommand extends Command {

    @Inject
    private AudioService audioService;

    public PlayCommand() {
        super("play", CommandCategory.MUSIC, "Plays music from the given url.");
    }

    @Override
    public void execute(final Message message, final String[] args) {
        final GuildVoiceState voiceState = message.getMember().getVoiceState();
        if (voiceState == null) {
            message.getChannel().sendMessage(Utils.createErrorEmbed("You are not connected to a voice channel.")).queue();
            return;
        }

        final VoiceChannel channel = voiceState.getChannel();
        final AudioTrackInfo info = this.audioService.play(message.getChannel(), channel, args[0]);

        message.getChannel().sendMessage(Utils.createInfoEmbed("I am now playing `" + info.title + "` from `" + info.author + "`.")).queue();
    }
}
