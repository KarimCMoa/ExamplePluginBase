package me.karim.utilities.design;

import me.karim.utilities.FontRenderer;
import org.bukkit.ChatColor;

public class Style {

    private static final int MAX_LENGTH = 53;
    private static final FontRenderer FONT_RENDERER = new FontRenderer();

    public static String FIRST_COLOR() {
        return CC.DARK_RED;
    }

    public static String SECOND_COLOR() {
        return CC.RED;
    }

    public static String THIRD_COLOR() {
        return CC.WHITE;
    }

    public static String SERVER_NAME() {
        return "CoreExample";
    }


    public static String translate(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static String getBorderLine() {
        int chatWidth = MAX_LENGTH / 10 * 9;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 100; i++) {
            sb.append("-");

            if (FONT_RENDERER.getWidth(sb.toString()) >= chatWidth) {
                break;
            }
        }

        return CC.GRAY + CC.STRIKE_THROUGH + sb.toString();
    }

}
