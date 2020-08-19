package net.getnova.discordbot.service.menu;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

@Service
public class MenuService extends ListenerAdapter {

    private static final String[] EMOTES = new String[]{"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"};
    private final Map<Long, IntConsumer> menus;

    public MenuService() {
        this.menus = new HashMap<>();
    }

    public void createMenu(final Message message, final IntConsumer select, final String[] options, final int count) {
        this.menus.put(message.getIdLong(), select);
        this.drawMenu(message, options, count);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        final long messageId = event.getMessageIdLong();
        final IntConsumer menu = this.menus.get(messageId);
        if (menu != null) {
            final String emoji = event.getReactionEmote().getEmoji();

            for (int i = 0; i < EMOTES.length; i++) {
                if (EMOTES[i].equals(emoji)) {
                    menu.accept(i);
                    return;
                }
            }
            this.menus.remove(messageId);
        }
    }

    private void drawMenu(final Message message, final String[] options, final int count) {
        final StringBuilder builder = new StringBuilder();
        final int length = Math.min(options.length, Math.min(count, EMOTES.length));
        for (int i = 0; i < length; i++) builder.append(i).append(". ").append(options[i]).append("\n");
        message.editMessage(builder.toString()).queue();
        for (int i = 0; i < length; i++) message.addReaction(EMOTES[i]).queue();
    }
}
