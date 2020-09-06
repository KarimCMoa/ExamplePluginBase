package me.karim.command.commands;

import me.karim.command.Command;
import me.karim.command.CommandHelp;
import me.karim.utilities.design.Style;
import org.bukkit.entity.Player;

public class CoreHelpCommands {

    @Command(names = "help")
    public static void help(Player player) {

        player.sendMessage(new String[]{
                Style.getBorderLine(),
                "/help admin, Global commands help",
                Style.getBorderLine()
        });
    }

    private static final CommandHelp[] ADMIN_HELP = new CommandHelp[]{
            new CommandHelp("/help test1", "Global Rank Help"),
            new CommandHelp("/help test2", "Broadcast Help Commands"),
            new CommandHelp("/help test3", "Global Punishments Help"),
            new CommandHelp("/help test4", "Global Cosmetic Help")
    };

    @Command(names = "help admin", permissionNode = "core.owner")
    public static void adminHelp(Player player) {

        player.sendMessage(Style.getBorderLine());
        for (CommandHelp help : ADMIN_HELP) {
            player.sendMessage(Style.translate(Style.SECOND_COLOR() + help.getSyntax() + " &7- " + Style.THIRD_COLOR() + help.getDescription()));
        }
        player.sendMessage(Style.getBorderLine());
    }
}
