package me.karim.chat.format;

import me.karim.chat.ChatFormat;
import org.bukkit.entity.Player;

public class DefaultChatFormat implements ChatFormat {

    @Override
    public String format(Player sender, Player receiver, String message) {
            Player player = sender;
            return player + ": " + message;
    }

}
