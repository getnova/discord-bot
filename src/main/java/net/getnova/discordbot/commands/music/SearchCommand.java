package net.getnova.discordbot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.getnova.discordbot.service.audio.AudioService;
import net.getnova.discordbot.service.audio.GuildMusicManager;
import net.getnova.discordbot.service.audio.LoadResultHandler;
import net.getnova.discordbot.service.command.Command;
import net.getnova.discordbot.service.command.CommandEvent;
import net.getnova.discordbot.service.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchCommand extends Command {

    private final AudioService audioService;
    private final MenuService menuService;

    @Autowired
    public SearchCommand(final AudioService audioService, final MenuService menuService) {
        super("search", new String[]{"seek", "s"}, " <query> ", true, "Searches for the given query on youtube.");
        this.audioService = audioService;
        this.menuService = menuService;
    }

    @Override
    public void execute(final CommandEvent event, final String[] args) {
        if (args.length == 0) {
            event.replyError("Please provide a query.");
            return;
        }

        final GuildVoiceState voiceState = event.getMember().getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            event.replyError("You are not connected to a voice channel.");
            return;
        }

        final VoiceChannel channel = voiceState.getChannel();
        final GuildMusicManager musicManager = this.audioService.getMusicManager(event.getGuild());
        if (musicManager.getChannel() != null && !musicManager.getChannel().equals(channel)) {
            event.replyError("I'm already connected to another voice channel.");
            return;
        }

        musicManager.setChannel(channel);

        final String query = String.join(" ", args);
        event.reply(event.getAuthor().getAsMention() + ", Loading... please wait", message -> this.audioService.loadAndPlay(musicManager, "ytsearch:" + query, new LoadResultHandler(
                track -> this.trackLoaded(message, event, track),
                list -> this.playlistLoaded(message, event, list),
                () -> this.nothingFound(message, event, query),
                cause -> this.onFuture(message, event, query, cause)
        )));
    }

    private void trackLoaded(final Message message, final CommandEvent event, final AudioTrack track) {
        message.editMessage(event.getAuthor().getAsMention() + ", Loaded track **" + track.getInfo().title
                + "** from **" + track.getInfo().author + "**.").queue();
    }

    private void playlistLoaded(final Message message, final CommandEvent event, final AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            this.menuService.createMenu(message, i -> {
                        final AudioTrack tack = playlist.getTracks().get(i);
                        this.audioService.getMusicManager(event.getGuild()).getScheduler().queue(tack);
                        message.clearReactions().queue();
                        this.trackLoaded(message, event, tack);
                    },
                    playlist.getTracks().stream().map(track -> "**" + track.getInfo().title + "** (" + track.getInfo().author + ")").toArray(String[]::new), 4);
        } else {
            message.editMessage(event.getAuthor().getAsMention() + ", Loaded playlist **" + playlist.getName()
                    + "** with **" + playlist.getTracks().size() + "** tracks.").queue();
        }
    }

    private void nothingFound(final Message message, final CommandEvent event, final String arg) {
        message.editMessage(event.getAuthor().getAsMention() + ", Nothing found for `" + arg + "`.").queue();
    }

    private void onFuture(final Message message, final CommandEvent event, final String arg, final Throwable cause) {
        message.editMessage(event.getAuthor().getAsMention() + "Error while loading music. The Error will be reported to us.").queue();
        log.error("Error while loading music. [\"" + arg + "\"]", cause);
    }
}
