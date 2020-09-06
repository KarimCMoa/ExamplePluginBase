package me.karim.chat;

import lombok.Getter;
import lombok.Setter;
import me.karim.chat.format.DefaultChatFormat;
import me.karim.utilities.design.Style;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class ChatManager {

    private static final Pattern URL_REGEX = Pattern.compile(
            "^(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-.][a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$");
    private static final Pattern IP_REGEX = Pattern.compile(
            "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([.,])){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    private static final List<String> LINK_WHITELIST = Arrays.asList(
            Style.SERVER_NAME() + ".cc",
            "eu." + Style.SERVER_NAME() + ".cc",
            "sa." + Style.SERVER_NAME() + ".cc",
            Style.SERVER_NAME() + ".club",
            "eu." + Style.SERVER_NAME() + ".club",
            "sa." + Style.SERVER_NAME() + ".club",
            Style.SERVER_NAME() + ".com",
            "eu." + Style.SERVER_NAME() + ".com",
            "sa." + Style.SERVER_NAME() + ".com",
            Style.SERVER_NAME() + ".us",
            "eu." + Style.SERVER_NAME() + ".us",
            "sa." + Style.SERVER_NAME() + ".us",
            Style.SERVER_NAME() + ".eu",
            "eu." + Style.SERVER_NAME() + ".eu",
            "sa." + Style.SERVER_NAME() + ".eu",
            Style.SERVER_NAME() + ".net",
            "eu." + Style.SERVER_NAME() + ".net",
            "sa." + Style.SERVER_NAME() + ".net",
            Style.SERVER_NAME() + ".org",
            "eu." + Style.SERVER_NAME() + ".org",
            "sa." + Style.SERVER_NAME() + ".org",
            Style.SERVER_NAME() + ".pw",
            "eu." + Style.SERVER_NAME() + ".pw",
            "sa." + Style.SERVER_NAME() + ".pw",
            "youtube.com",
            "youtu.be",
            "discord.gg",
            "twitter.com",
            "prnt.sc",
            "gyazo.com",
            "imgur.com"
    );
    private final int delayTime = 3;
    @Setter
    private ChatFormat chatFormat = new DefaultChatFormat();
    private boolean chatMuted = false;

    public void toggleMuteChat() {
        this.chatMuted = !this.chatMuted;
    }

    public boolean shouldFilter(String message) {
        String msg = message.toLowerCase()
                .replace("3", "e")
                .replace("1", "i")
                .replace("!", "i")
                .replace("@", "a")
                .replace("7", "t")
                .replace("0", "o")
                .replace("5", "s")
                .replace("8", "b")
                .replaceAll("\\p{Punct}|\\d", "").trim();

        String[] words = msg.trim().split(" ");

        for (String word : message.replace("(dot)", ".").replace("[dot]", ".").trim().split(" ")) {
            boolean continueIt = false;

            for (String phrase : ChatManager.LINK_WHITELIST) {
                if (word.toLowerCase().contains(phrase)) {
                    continueIt = true;
                    break;
                }
            }

            if (continueIt) {
                continue;
            }

            Matcher matcher = ChatManager.IP_REGEX.matcher(word);

            if (matcher.matches()) {
                return true;
            }

            matcher = ChatManager.URL_REGEX.matcher(word);

            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}
